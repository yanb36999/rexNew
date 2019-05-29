package com.zmcsoft.rex.pay;

import com.alibaba.fastjson.JSON;
import lombok.*;
import org.hswebframework.expands.request.RequestBuilder;
import org.hswebframework.expands.request.SimpleRequestBuilder;
import org.hswebframework.expands.request.http.HttpRequest;
import org.hswebframework.expands.request.http.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

public class AsyncCallbackSupport {

    private static Logger logger = LoggerFactory.getLogger("business.pay.callback");

    private static final Queue<CallbackInfo> CALLBACK_INFO_QUEUE = new ConcurrentLinkedDeque<>();
    private static final Queue<CallbackInfo> RETRY_INFO_QUEUE = new ConcurrentLinkedDeque<>();

    private static ScheduledExecutorService executorService = Executors.newScheduledThreadPool(10);

//    protected Map<String, CallbackCache> cache = new ConcurrentHashMap<>();

    protected static RequestBuilder requestBuilder = new SimpleRequestBuilder();

    private static boolean running = true;

    static {

        java.util.function.Consumer<CallbackInfo> callbackConsumer = (callbackInfo) -> {
            try {
                logger.info("调用支付回调:{}", callbackInfo);
                callbackInfo.doCallback();
                callbackInfo.successCallback.run();
            } catch (Exception e) {
                logger.warn("回调失败:{},重入队列,等待重试!", callbackInfo, e);
                callbackInfo.errorInfo.add(e.getMessage());
                callbackInfo.retryTime.incrementAndGet();
                //加入重试队列
                RETRY_INFO_QUEUE.add(callbackInfo);
            }
        };

        Thread retry = new Thread(() -> {
            for (; running; ) {
                CallbackInfo retryCall = RETRY_INFO_QUEUE.poll();
                if (retryCall == null) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                if (retryCall.retryTime.get() > 10) {
                    logger.error("重试回调超过10次:{}", retryCall);
                    if (retryCall.errorCallback != null) {
                        retryCall.errorCallback.run();
                    }
                    continue;
                }
                //等待重试
                executorService.schedule(() -> callbackConsumer.accept(retryCall), retryCall.retryTime.get(), TimeUnit.MINUTES);
            }
        });

        Thread thread = new Thread(() -> {
            CallbackInfo callbackInfo;
            for (; running; ) {
                callbackInfo = CALLBACK_INFO_QUEUE.poll();
                if (null == callbackInfo) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                CallbackInfo finalCall=callbackInfo;
                //使用线程池执行回调
                executorService.execute(() -> callbackConsumer.accept(finalCall));
                //callbackConsumer.accept(callbackInfo);
            }
            while ((callbackInfo = CALLBACK_INFO_QUEUE.poll()) != null) {
                callbackConsumer.accept(callbackInfo);
            }
        });

        thread.setName("pay-callback");

        retry.setName("pay-callback-retry");
        thread.start();
        retry.start();
    }

    @PreDestroy
    public void shutdown() {
        running = false;
    }

    public void addHttpCallback(CallbackCache cache, Consumer<HttpRequest> requestConsumer, Runnable success, Runnable error) {
        addCallback(new Callback() {
            @Override
            public void call() throws Exception {
                HttpRequest request = requestBuilder
                        .http(cache.getCallbackUrl())
                        .params(cache.getRequest().getParameters());

                requestConsumer.accept(request);
                Response response = request.post();
                if (response.getCode() != 200) {
                    throw new RuntimeException("call back return http code :" + response.getCode() + ",content:" + response.asString());
                }
            }

            @Override
            public String toString() {
                return cache.toString();
            }
        }, success, error);
    }

    public void addCallback(Callback doCallback, Runnable onSuccess, Runnable error) {
        CallbackInfo callbackInfo = new CallbackInfo();
        callbackInfo.callback = doCallback;
        callbackInfo.successCallback = onSuccess;
        callbackInfo.errorCallback = error;
        CALLBACK_INFO_QUEUE.add(callbackInfo);
    }

    public interface Callback {
        void call() throws Exception;
    }

    class CallbackInfo {

        private AtomicLong retryTime = new AtomicLong(0);

        private Runnable successCallback;

        private Runnable errorCallback;

        private Callback callback;

        private List<String> errorInfo = new ArrayList<>();

        private void doCallback() throws Exception {
            callback.call();
        }

        public List<String> getErrorInfo() {
            return errorInfo;
        }

        @Override
        public String toString() {
            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("retryTime", retryTime.longValue());
            objectMap.put("callback", callback.toString());
            objectMap.put("retryTime", retryTime.longValue());

            return JSON.toJSONString(objectMap);
        }
    }

    @ToString
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CallbackCache {

        private String redirectUrl;

        private String callbackUrl;

        private PayRequest request;

    }
}
