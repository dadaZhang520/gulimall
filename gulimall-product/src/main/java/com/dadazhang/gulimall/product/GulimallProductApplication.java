package com.dadazhang.gulimall.product;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * SpringCache自定义属性
 * 1.) SpringCache: 默认生成的Key  catalogLevel1::SimpleKey []
 *     自定义生成key可以在Cacheable定义key的值   @Cacheable(value = "catalogLevel1",key = "'categoryLevel1'")
 *     自定义SPEL表达式来自定以key :
 *     https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/integration.html#cache-spel-context
 * 2.) SpringCache：默认的key的ttl  TTL:-1  永不过期
 *     自定义key的TTL 可以通过配置  spring.cache.redis.time-to-live=360000 设置
 * 3.）SpringCache：默认生成Value值，是通过jdk的序列化生成的格式
 *     自定义value的序列化格式（JSON格式），配置 RedisCacheConfiguration类来进行配置value的序列化规则
 */

@EnableRedisHttpSession
@EnableFeignClients("com.dadazhang.gulimall.product.feign")
@EnableDiscoveryClient
@MapperScan("com.dadazhang.gulimall.product.dao")
@SpringBootApplication
public class GulimallProductApplication {

    public static void main(String[] args) {
        SpringApplication.run(GulimallProductApplication.class, args);
    }

}
