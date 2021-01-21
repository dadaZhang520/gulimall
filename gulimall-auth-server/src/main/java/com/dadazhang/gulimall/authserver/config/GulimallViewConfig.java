package com.dadazhang.gulimall.authserver.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class GulimallViewConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
/*        registry.addViewController("/").setViewName("login");
        registry.addViewController("/login.html").setViewName("login");*/
        registry.addViewController("/register.html").setViewName("register");
    }
}
