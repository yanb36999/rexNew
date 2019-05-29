package com.zmcsoft.rex.message;

import lombok.extern.slf4j.Slf4j;
import org.hswebframework.web.ThreadLocalUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Slf4j
public abstract class AbstractAsyncMessageSender implements MessageSender {

    private static final Queue<MessageSender> queueJob = new ConcurrentLinkedDeque<>();

    private static boolean running = false;

    private static final ExecutorService executorService;

    static {
        int threadNumber = Runtime.getRuntime().availableProcessors() * 2;


        executorService = Executors.newFixedThreadPool(threadNumber);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            log.info("结束消息推送,剩余数量:{}", queueJob.size());

        }));
        Thread jobExecutor = new Thread(() -> {
            while (!running) {
                MessageSender sender = queueJob.poll();
                if (sender == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    continue;
                }
                executorService.execute(sender::send);
            }

        });

        jobExecutor.setName("AbstractAsyncMessageSender");
        jobExecutor.start();
    }

    protected abstract boolean doSend();

    @Override
    public boolean send() {
       // Map<String, Object> locals = new HashMap<>(ThreadLocalUtils.getAll());
        // locals.forEach(ThreadLocalUtils::put);
// try {
//} finally {
//ThreadLocalUtils.clear();
//   }
        queueJob.add(this::doSend);
        return true;
    }

}
