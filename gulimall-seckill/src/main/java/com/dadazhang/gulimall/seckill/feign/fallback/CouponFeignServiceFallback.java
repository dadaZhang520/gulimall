package com.dadazhang.gulimall.seckill.feign.fallback;

import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.seckill.feign.CouponFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CouponFeignServiceFallback implements CouponFeignService {
    @Override
    public R threeDaysSeckillSku() {
        log.info("coupon服务已熔断。。。。。。");
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }
}
