package com.dadazhang.gulimall.product.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("gulimall.thread.pool")
@Component
@Data
public class ThreadPoolExecutorPropertiesConfig {

    private Integer codePoolSize;

    private Integer maximumPoolSize;

    private Integer keepAliveTime;
}
