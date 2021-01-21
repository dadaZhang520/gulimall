package com.dadazhang.gulimall.product.feign.fallback;

import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SeckillFeignServiceFallback implements SeckillFeignService {

    @Override
    public R getSeckillBySkuId(Long skuId) {
        log.info("seckill服务已熔断。。。。。。");
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }
}
