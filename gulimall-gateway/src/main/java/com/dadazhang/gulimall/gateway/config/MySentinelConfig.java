package com.dadazhang.gulimall.gateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.GatewayCallbackManager;
import com.alibaba.fastjson.JSON;
import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.utils.R;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Configuration
public class MySentinelConfig {

    public MySentinelConfig() {
        GatewayCallbackManager.setBlockHandler((serverWebExchange, throwable) -> {
                    R error = R.error(BizCodeEnum.CUSTOM_EXCEPTION.getCode(), BizCodeEnum.CUSTOM_EXCEPTION.getMessage());
                    String json = JSON.toJSONString(error);
                    return ServerResponse.ok().body(Mono.just(json), String.class);
                }
        );
    }
}
