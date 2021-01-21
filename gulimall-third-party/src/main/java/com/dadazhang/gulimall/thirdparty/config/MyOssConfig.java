package com.dadazhang.gulimall.thirdparty.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.dadazhang.gulimall.thirdparty.entity.OssEntity;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyOssConfig {

    @Bean
    public OSS createOss(OssEntity ossEntity) {
        OSS ossClient = new OSSClientBuilder().build(ossEntity.getEndpoint(), ossEntity.getAccessKey(), ossEntity.getSecretKey());
        return ossClient;
    }
}
