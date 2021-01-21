package com.dadazhang.gulimall.order.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient("gulimall-cart")
public interface CartFeignService {

    @GetMapping("/user/cart/item")
    R getUserCartItem();
}
