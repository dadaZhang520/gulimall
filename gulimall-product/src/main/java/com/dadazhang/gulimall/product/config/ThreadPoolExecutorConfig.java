package com.dadazhang.gulimall.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor newThreadPoolExecutor(ThreadPoolExecutorPropertiesConfig threadPoolExecutorProperties) {

        return
                new ThreadPoolExecutor(
                        threadPoolExecutorProperties.getCodePoolSize(),
                        threadPoolExecutorProperties.getMaximumPoolSize(),
                        threadPoolExecutorProperties.getKeepAliveTime(),
                        TimeUnit.SECONDS,
                        new LinkedBlockingDeque<>(10000),
                        Executors.defaultThreadFactory(),
                        new ThreadPoolExecutor.AbortPolicy());
    }
}
