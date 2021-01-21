package com.dadazhang.common.to.mq;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillOrderTo {

    private String orderSn;
    private Long memberId;
    private Long skuId;
    private Long sessionId;
    private BigDecimal price;
    private Integer num;

}
