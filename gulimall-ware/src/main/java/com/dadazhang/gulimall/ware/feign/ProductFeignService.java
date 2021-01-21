package com.dadazhang.gulimall.ware.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/skuinfo/info/{skuId}")
    R skuById(@PathVariable("skuId") Long skuId);
}
