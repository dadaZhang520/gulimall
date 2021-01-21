package com.dadazhang.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("gulimall.thread")
public class ThreadPoolExecutorProperties {

    private Integer coreSize;
    private Integer maxSize;
    private Integer keepAliveTime;

}
