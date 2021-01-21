package com.dadazhang.gulimall.order.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("/product/spuinfo/info/sku/id")
    R getSpuInfoBySkuId(@RequestParam Long skuId);

    @GetMapping("/product/spuinfodesc/info/{spuId}")
    R getDescInfoById(@PathVariable("spuId") Long spuId);

    @GetMapping("/product/brand/info/{brandId}")
    R getBrandInfo(@PathVariable("brandId") Long brandId);

    @GetMapping("/product/skuinfo/info/{skuId}")
    R getSkuInfoById(@PathVariable("skuId") Long skuId) ;
}
