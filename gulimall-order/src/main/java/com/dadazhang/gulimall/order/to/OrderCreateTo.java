package com.dadazhang.gulimall.order.to;

import com.dadazhang.gulimall.order.entity.OrderEntity;
import com.dadazhang.gulimall.order.entity.OrderItemEntity;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderCreateTo {

    private OrderEntity order;

    private List<OrderItemEntity> items;

    private BigDecimal payPrice;

    private BigDecimal farePrice;

}
