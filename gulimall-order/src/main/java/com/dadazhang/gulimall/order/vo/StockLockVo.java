package com.dadazhang.gulimall.order.vo;

import com.dadazhang.gulimall.order.entity.OrderEntity;
import lombok.Data;

import java.util.List;

@Data
public class StockLockVo {

    private OrderEntity order;

    private List<StockLockItemVo> orderItem;

    @Data
    public static class StockLockItemVo {
        private Long skuId;

        private Integer lockNum;
    }
}
