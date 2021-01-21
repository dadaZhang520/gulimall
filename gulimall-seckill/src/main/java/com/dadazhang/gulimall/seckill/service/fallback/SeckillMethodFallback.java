package com.dadazhang.gulimall.seckill.service.fallback;


import com.dadazhang.gulimall.seckill.to.SeckillRedisTo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SeckillMethodFallback {

    public static List<SeckillRedisTo> blockHandlerGetCurrentSeckillSku(Throwable t) {
        log.error("注解回调。。。");
        return null;
    }
}
