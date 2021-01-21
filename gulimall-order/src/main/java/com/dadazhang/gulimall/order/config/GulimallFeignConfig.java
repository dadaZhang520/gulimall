package com.dadazhang.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * feign远程调用丢失请求header的配置
 * 配置Feign创建RequestTemplate时会进入RequestInterceptor拦截器
 * 操作拦截器将浏览器的Cookie复制到RequestTemplate中
 */
@Configuration
public class GulimallFeignConfig {

    @Bean
    public RequestInterceptor requestTemplate() {

        return requestTemplate -> {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                HttpServletRequest request = requestAttributes.getRequest();
                String cookie = request.getHeader("Cookie");
                requestTemplate.header("Cookie", cookie);
            }
        };
    }

}
