package com.dadazhang.gulimall.seckill.feign;

import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.seckill.feign.fallback.CouponFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(value = "gulimall-coupon", fallback = CouponFeignServiceFallback.class)
public interface CouponFeignService {

    @GetMapping("/coupon/seckillsession/threeDay/seckill/session")
    R threeDaysSeckillSku();
}
