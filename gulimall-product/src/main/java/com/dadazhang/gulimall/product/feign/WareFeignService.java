package com.dadazhang.gulimall.product.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/skus/has/stock")
    R skuHasStockBySkuIds(@RequestParam Long[] skuIds);

    @PostMapping("/ware/waresku/sku/has/stock")
    boolean skuHasStockBySkuId(@RequestParam Long skuId);
}
