package com.dadazhang.gulimall.cart.config.lcart.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("gulimall.thread.pool")
@Component
@Data
public class ThreadPoolExecutorProperties {

    private Integer codePoolSize;

    private Integer maximumPoolSize;

    private Integer keepAliveTime;
}
