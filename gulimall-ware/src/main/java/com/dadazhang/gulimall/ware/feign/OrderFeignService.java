package com.dadazhang.gulimall.ware.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient("gulimall-order")
public interface OrderFeignService {

    @GetMapping("/order/order/get/{orderSn}")
    R getOrderByOrderSn(@PathVariable("orderSn") String orderSn);
}
