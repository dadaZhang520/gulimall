package com.dadazhang.gulimall.order.feign;

import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.order.vo.HasStockVo;
import com.dadazhang.gulimall.order.vo.StockLockVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("gulimall-ware")
public interface WareFeignService {

    @PostMapping("/ware/waresku/order/has/stock/")
    R skuHasStockBySkuIds(@RequestBody List<HasStockVo> hasStockVos);

    @GetMapping("/ware/wareinfo/fare")
    R getFare(@RequestParam("addrId") Long addrId);

    @PostMapping("/ware/waresku/lock/stock")
    R lockStock(@RequestBody StockLockVo stockLockVo);

    @PostMapping("/ware/waresku/order/finish/handler")
    R orderFinishHandler(@RequestParam String orderSn);
}
