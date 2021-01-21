package com.dadazhang.gulimall.order.vo;

import com.dadazhang.gulimall.order.entity.OrderEntity;
import lombok.Data;

@Data
public class OrderSubmitResponseVo {

    private Integer code;

    private OrderEntity order;
}
