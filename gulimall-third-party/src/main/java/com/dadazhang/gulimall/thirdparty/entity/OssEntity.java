package com.dadazhang.gulimall.thirdparty.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties("alibaba.cloud.oss")
public class OssEntity {

    private String accessKey;

    private String secretKey;

    private String endpoint;

    private String bucket;
}
