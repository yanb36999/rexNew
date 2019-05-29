package com.zmcsoft.rex.redission;

import org.hswebframework.web.authorization.token.DefaultUserTokenManager;
import org.hswebframework.web.authorization.token.SimpleUserToken;
import org.hswebframework.web.authorization.token.UserTokenManager;
import org.nustaq.serialization.FSTConfiguration;
import org.redisson.Redisson;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RedissonClient;
import org.redisson.codec.FstCodec;
import org.redisson.config.Config;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.redisson.spring.session.config.EnableRedissonHttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author zhouhao
 * @since redis session manager
 */
@EnableRedissonHttpSession(maxInactiveIntervalInSeconds = 3600)
@Configuration
public class RedissionCacheAndSessionAutoConfiguration {

    @Value("${redission.host:'redis://127.0.0.1:6379'}")
    private String host = "redis://127.0.0.1:6379";

    public void setHost(String host) {
        this.host = host;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer().setAddress(host);
        return Redisson.create(config);
    }

    @Bean
    public UserTokenManager userTokenManager(RedissonClient redissonClient) {
        LocalCachedMapOptions<String, SimpleUserToken> localCachedMapOptions =
                LocalCachedMapOptions.<String, SimpleUserToken>defaults()
                        .maxIdle(10, TimeUnit.MINUTES)
                        .timeToLive(5, TimeUnit.MINUTES)
                        .cacheSize(2048);
        FstCodec codec = fstCodec();

        ConcurrentMap<String, SimpleUserToken> repo = redissonClient.getMap("hsweb.user-token", codec, localCachedMapOptions);
        ConcurrentMap<String, Set<String>> userRepo = redissonClient.getMap("hsweb.user-token-user", codec);

        return new DefaultUserTokenManager(repo, userRepo) {
            @Override
            protected Set<String> getUserToken(String userId) {
                userRepo.computeIfAbsent(userId, u -> new HashSet<>());

                return redissonClient.getSet("hsweb.user-token-" + userId, codec);
            }
        };
    }

    @Bean
    public FstCodec fstCodec(){
        FSTConfiguration def = FSTConfiguration.createDefaultConfiguration();
        def.setClassLoader(this.getClass().getClassLoader());
        def.setForceSerializable(true);

        FstCodec codec = new FstCodec(def);
        return codec;
    }

    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {


        return new RedissonSpringCacheManager(redissonClient, new ConcurrentHashMap<>(256), fstCodec());
    }

}
