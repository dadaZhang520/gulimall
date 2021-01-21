package com.dadazhang.gulimall.product.feign;

import com.dadazhang.common.to.SkuReductionTo;
import com.dadazhang.common.to.SpuBoundTo;
import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-coupon")
public interface CouponFeignService {

    @PostMapping("/coupon/spubounds/save")
    R saveSpuBound(@RequestBody SpuBoundTo spuBoundTo);

    @PostMapping("/coupon/skufullreduction/sku/reduction")
    R saveSkuReduction(@RequestBody SkuReductionTo skuReductionTo);


}
