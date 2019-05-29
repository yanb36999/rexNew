package com.zmcsoft.rex.logging;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoggingRedissonConfiguration {

    @Value("${redission.logging.host:'redis://127.0.0.1:6379'}")
    private String host = "redis://127.0.0.1:6379";

    public RedissonClient loggingRedissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(host);
        return Redisson.create(config);
    }

    @Bean
    public RedisLoggerListener redisLoggerListener() {
        return new RedisLoggerListener(loggingRedissonClient());
    }


}
