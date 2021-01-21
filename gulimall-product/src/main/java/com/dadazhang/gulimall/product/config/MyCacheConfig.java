package com.dadazhang.gulimall.product.config;

import com.alibaba.fastjson.support.spring.GenericFastJsonRedisSerializer;
import org.springframework.boot.autoconfigure.cache.CacheProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@EnableCaching//开启SpringCache
@Configuration
public class MyCacheConfig {

    /**
     * 获取application.yml 中的配置属性的方式
     * 1.） @EnableConfigurationProperties({CacheProperties.class}) 程序会自动加载CacheProperties.class类
     *      使用 @Autowired CacheProperties cacheProperties; 进行注入
     * 2.） RedisCacheConfiguration createRedisCacheConfiguration(CacheProperties cacheProperties)
     *      程序会自动创建实例，直接使用
     */


    @Bean
    RedisCacheConfiguration createRedisCacheConfiguration(CacheProperties cacheProperties){
        //配置RedisCacheConfiguration
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig();

        //获取application.yml中的配置
        CacheProperties.Redis redisProperties = cacheProperties.getRedis();

        //配置Key的序列化
        config =  config.serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()));

        //配置Value的序列化
        config = config.serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericFastJsonRedisSerializer()));

        //判断配置文件中的值
        if (redisProperties.getTimeToLive() != null) {
            config = config.entryTtl(redisProperties.getTimeToLive());
        }

        if (redisProperties.getKeyPrefix() != null) {
            config = config.prefixCacheNameWith(redisProperties.getKeyPrefix());
        }

        if (!redisProperties.isCacheNullValues()) {
            config = config.disableCachingNullValues();
        }

        if (!redisProperties.isUseKeyPrefix()) {
            config = config.disableKeyPrefix();
        }

        return config;
    }
}
