package com.dadazhang.gulimall.product.feign;

import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.product.feign.fallback.SeckillFeignServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "gulimall-seckill",fallback = SeckillFeignServiceFallback.class)
public interface SeckillFeignService {

    @GetMapping("/get/seckill/bySkuId")
    R getSeckillBySkuId(@RequestParam Long skuId);
}
