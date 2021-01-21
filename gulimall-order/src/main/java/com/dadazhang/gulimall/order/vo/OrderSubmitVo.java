package com.dadazhang.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderSubmitVo {

    private Long addressId;  //发货地址
    private BigDecimal payPrice; //应付价格
    private Integer payType; //支付方式
    private String orderToken; //防重令牌
    private String note; //订单描述
}
