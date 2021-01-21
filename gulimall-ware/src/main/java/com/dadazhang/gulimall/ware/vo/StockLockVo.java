package com.dadazhang.gulimall.ware.vo;

import com.dadazhang.common.to.OrderTo;
import lombok.Data;

import java.util.List;

@Data
public class StockLockVo {

    private OrderTo order;

    private List<StockLockItemVo> orderItem;

    @Data
    public static class StockLockItemVo {
        private Long skuId;

        private Integer lockNum;
    }
}
