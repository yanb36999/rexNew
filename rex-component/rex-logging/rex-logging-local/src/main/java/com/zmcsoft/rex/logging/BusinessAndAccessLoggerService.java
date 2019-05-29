package com.zmcsoft.rex.logging;

import com.alibaba.fastjson.JSON;
import com.zmcsoft.rex.logging.access.AccessLoggerDao;
import com.zmcsoft.rex.logging.access.entity.AccessLogger;
import com.zmcsoft.rex.logging.business.BusinessLogger;
import com.zmcsoft.rex.logging.business.BusinessLoggerListener;
import com.zmcsoft.rex.logging.business.dao.BusinessLoggerDao;
import org.apache.commons.codec.digest.DigestUtils;
import org.hswebframework.utils.StringUtils;
import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.authorization.Authentication;
import org.hswebframework.web.boost.aop.context.MethodInterceptorHolder;
import org.hswebframework.web.datasource.DataSourceHolder;
import org.hswebframework.web.logging.AccessLoggerInfo;
import org.hswebframework.web.logging.AccessLoggerListener;
import org.hswebframework.web.service.DefaultDSLUpdateService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@ConfigurationProperties(prefix = "com.zmcsoft.logger")
@Service
public class BusinessAndAccessLoggerService implements BusinessLoggerListener, AccessLoggerListener {

    private Logger saveLogger = LoggerFactory.getLogger("logger.save");

    private Queue<BusinessLogger> businessLoggerQueue = new ConcurrentLinkedDeque<>();

    private Queue<AccessLogger> accessLoggerQueue = new ConcurrentLinkedDeque<>();

    private Queue<AccessLogger> accessLoggerOkQueue = new ConcurrentLinkedDeque<>();

    private Map<String, CountDownLatch> accessLoggerCountDown = new ConcurrentHashMap<>();

    private String dataSourceId = null;

    private boolean shutdown = false;

    private CountDownLatch shutdownLatch = new CountDownLatch(3);

    @Value("${hsweb.app.name:default}")
    private String app = "default";

    @Value("${hsweb.app.version:1.0.0}")
    private String version = "1.0.0";

    @Autowired
    private BusinessLoggerDao businessLoggerDao;

    @Autowired
    private AccessLoggerDao accessLoggerDao;

    public String getAppFullName() {
        return app + ":" + version;
    }

    @PreDestroy
    public void destroy() {
        shutdown = true;
        try {
            shutdownLatch.await(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void trySwitchDataSource() {
        if (dataSourceId != null) {
            DataSourceHolder.switcher().use(dataSourceId);
        }
    }

    private void tryFallBackDataSource() {
        if (dataSourceId != null) {
            DataSourceHolder.switcher().useLast();
        }
    }

    private <L> Runnable createJob(Queue<L> queue, Consumer<L> consumer) {
        return () -> {
            L logger = queue.poll();
            if (logger == null) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignore) {
                }
                return;
            }
            trySwitchDataSource();
            try {
                consumer.accept(logger);
            } finally {
                tryFallBackDataSource();
            }
        };
    }

    private Thread createThread(Runnable runnable) {
        return new Thread(() -> {
            while (!shutdown) {
                runnable.run();
            }
            //
            while (businessLoggerQueue.size() > 0) {
                runnable.run();
            }
            shutdownLatch.countDown();
        });
    }

    @PostConstruct
    public void init() {
        //开启2个线程去消费日志队列中的日志

        Runnable businessLoggerJob = createJob(businessLoggerQueue, logger -> {
            try {
                businessLoggerDao.insert(logger);
            } catch (Exception e) {

                saveLogger.error("保存业务日志失败:\n{}\n", JSON.toJSON(logger), e);
            }
        });

        Thread businessLoggerThread = createThread(businessLoggerJob);

        Runnable accessLoggerJob = createJob(accessLoggerQueue, logger -> {
            CountDownLatch latch = accessLoggerCountDown.get(logger.getId());
            try {
                accessLoggerDao.insert(logger);
            } catch (Exception e) {
                //
                saveLogger.error("保存访问日志失败:\n{}\n", JSON.toJSON(logger), e);
            } finally {
                if (null != latch) {
                    latch.countDown();
                }
            }
        });
        Runnable accessLoggerOkJob = createJob(accessLoggerOkQueue, logger -> {
            //倒计时
            CountDownLatch latch = accessLoggerCountDown.get(logger.getId());

            try {
                //等待倒计时结束,倒计时结束才代表已经insert了
                latch.await(10, TimeUnit.MINUTES);
                int i = DefaultDSLUpdateService.createUpdate(accessLoggerDao)
                        .set("response", logger.getResponse())
                        .set("responseTime", logger.getResponseTime())
                        .set("requestUserId", logger.getRequestUserId())
                        .set("requestUserName", logger.getRequestUserName())
                        .where("id", logger.getId())
                        .exec();
                if (i == 0) {
                    saveLogger.error("更新访问日志失败,日志不存在:{}", JSON.toJSON(logger));
                }
            } catch (Exception e) {
                //
                saveLogger.error("更新访问日志失败:\n{}\n", JSON.toJSON(logger), e);
            } finally {
                accessLoggerCountDown.remove(logger.getId());
            }
        });

        Thread accessLoggerOkThread = createThread(accessLoggerOkJob);

        Thread accessLoggerThread = createThread(accessLoggerJob);

        accessLoggerThread.setName("access-logger");
        businessLoggerThread.setName("business-logger");

        accessLoggerOkThread.setName("access-logger-done");

        accessLoggerThread.start();
        businessLoggerThread.start();
        accessLoggerOkThread.start();
    }


    @Override
    public void onLogger(BusinessLogger logger) {
        //request id 为访问日志的id
        String requestId = ThreadLocalUtils.get(AccessLoggerInfo.class + ".id", () -> "-1");
        logger.setApp(getAppFullName());
        logger.setRequestId(requestId);
        if (shutdown) {
            saveLogger.error("日志服务已关闭,无法再保存业务日志:{}", JSON.toJSONString(logger));
            return;
        }
        businessLoggerQueue.add(logger);
    }

    @Override
    public void onLogBefore(AccessLoggerInfo accessLoggerInfo) {
        String id = DigestUtils.md5Hex(accessLoggerInfo.getId());
        accessLoggerCountDown.put(id, new CountDownLatch(1));

        ThreadLocalUtils.put(AccessLoggerInfo.class + ".id", id);

        String[] parameterNames = MethodInterceptorHolder.nameDiscoverer.getParameterNames(accessLoggerInfo.getMethod());
        Class[] parameterTypes = accessLoggerInfo.getMethod().getParameterTypes();
        StringJoiner joiner = new StringJoiner(",");
        for (int i = 0; i < parameterNames.length; i++) {
            joiner.add(parameterTypes[i].getSimpleName() + " " + parameterNames[i]);
        }
//        LoggerFactory.getLogger("business.test").trace("test:{}",1);
        Authentication authentication = Authentication.current().orElse(null);

        AccessLogger logger = AccessLogger.builder()
                .app(getAppFullName())
                .className(accessLoggerInfo.getClass().getName())
                .methodName(accessLoggerInfo.getMethod().getName() + "(" + joiner.toString() + ")")
                .httpHeader(JSON.toJSONString(accessLoggerInfo.getHttpHeaders()))
                .httpMethod(accessLoggerInfo.getHttpMethod())
                .ipAddress(accessLoggerInfo.getIp())
                .action(Optional.ofNullable(accessLoggerInfo.getAction()).orElse(accessLoggerInfo.getMethod().getName()))
                .requestTime(accessLoggerInfo.getRequestTime())
                .responseTime(-1L)
                .parameters(getParameters(accessLoggerInfo))
                .requestUrl(accessLoggerInfo.getUrl())
                //.response(getResponse(accessLoggerInfo))
                .build();
        logger.setId(id);
        if (authentication != null) {
            logger.setRequestUserId(authentication.getUser().getId());
            logger.setRequestUserName(authentication.getUser().getName());
        }
        if (shutdown) {
            saveLogger.error("日志服务已关闭,无法再保存访问日志:{}", JSON.toJSONString(logger));
            return;
        }
        accessLoggerQueue.add(logger);
    }

    private String getParameters(AccessLoggerInfo accessLoggerInfo) {

        return null;
    }

    private String getResponse(AccessLoggerInfo loggerInfo) {

        return null;
    }

    @Override
    public void onLogger(AccessLoggerInfo accessLoggerInfo) {
        String id = DigestUtils.md5Hex(accessLoggerInfo.getId());
        Authentication authentication = Authentication.current().orElse(null);

        AccessLogger logger = AccessLogger.builder()
                .responseTime(accessLoggerInfo.getResponseTime())
                .response(getResponse(accessLoggerInfo))
                .build();
        logger.setId(id);
        Throwable exception = accessLoggerInfo.getException();
        if(exception!=null){
            logger.setResponse(StringUtils.throwable2String(exception));
        }
        if (authentication != null) {
            logger.setRequestUserId(authentication.getUser().getId());
            logger.setRequestUserName(authentication.getUser().getName());
        }
        if (shutdown) {
            saveLogger.error("日志服务已关闭,无法再保存业务日志:{}", JSON.toJSONString(logger));
            return;
        }
        accessLoggerOkQueue.add(logger);

    }

    public void setDataSourceId(String dataSourceId) {
        this.dataSourceId = dataSourceId;
    }

    public String getDataSourceId() {
        return dataSourceId;
    }
}
