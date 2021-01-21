package com.dadazhang.gulimall.order.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderItemVo {
    private Long skuId;
    private String title;
    private String img;
    private BigDecimal price;
    private Integer count;
    private BigDecimal totalPrice;
    private List<String> saleAttr;
}