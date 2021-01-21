package com.dadazhang.gulimall.cart.config.lcart.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient("gulimall-product")
public interface ProductFeignService {

    @GetMapping("product/skuinfo/info/{skuId}")
    R info(@PathVariable("skuId") Long skuId);

    @GetMapping("product/skusaleattrvalue/sku/sale/attr/list")
    R getSkuSaleAttrBySkuId(@RequestParam Long skuId);

    @GetMapping("product/skuinfo/price/{id}")
    BigDecimal getPriceById(@PathVariable("id") Long skuId);
}
