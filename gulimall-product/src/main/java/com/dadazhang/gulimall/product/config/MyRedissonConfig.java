package com.dadazhang.gulimall.product.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRedissonConfig {

    @Bean
    public RedissonClient createRedissonClient(){
        //配置redisson的Config
        Config config = new Config();

        //设置redis服务器地址
        config.useSingleServer().setAddress("redis://192.168.240.128:6379");

        //通过config配置创建redissonClient
        RedissonClient redissonClient = Redisson.create(config);

        return redissonClient;
    }


}
