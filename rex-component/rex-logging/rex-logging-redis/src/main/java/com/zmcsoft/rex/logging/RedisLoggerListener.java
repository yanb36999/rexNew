package com.zmcsoft.rex.logging;

import com.alibaba.fastjson.JSON;
import com.zmcsoft.rex.logging.business.BusinessLogger;
import com.zmcsoft.rex.logging.business.BusinessLoggerListener;
import org.hswebframework.web.ThreadLocalUtils;
import org.hswebframework.web.logging.AccessLoggerInfo;
import org.hswebframework.web.logging.AccessLoggerListener;
import org.redisson.api.RedissonClient;

import java.util.Map;

public class RedisLoggerListener implements BusinessLoggerListener, AccessLoggerListener {

    private RedissonClient redissonClient;


    public RedisLoggerListener(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public void onLogger(BusinessLogger logger) {
        String accessId = ThreadLocalUtils.get(AccessLoggerInfo.class.getName() + "_id", () -> "-1");
        logger.setRequestId(accessId);
        redissonClient
                .getQueue("business-logger")
                .add(JSON.toJSONString(logger));
    }

    @Override
    public void onLogger(AccessLoggerInfo accessLoggerInfo) {
        Map<String, Object> map = accessLoggerInfo.toSimpleMap(noSer -> noSer.getClass().getName());
        redissonClient
                .getQueue("access-logger")
                .add(JSON.toJSONString(map));
    }

    @Override
    public void onLogBefore(AccessLoggerInfo loggerInfo) {
        //在开始访问的时候调用，此时还没有响应结果，时间等数据
        ThreadLocalUtils.put(AccessLoggerInfo.class.getName() + "_id", loggerInfo.getId());


    }
}
