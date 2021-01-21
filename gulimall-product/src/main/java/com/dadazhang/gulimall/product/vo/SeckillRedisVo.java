package com.dadazhang.gulimall.product.vo;

import com.dadazhang.gulimall.product.entity.SkuInfoEntity;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillRedisVo {

    private Long id;

    private Long promotionId;

    private Long promotionSessionId;

    private Long skuId;

    private BigDecimal seckillPrice;

    private BigDecimal seckillCount;

    private BigDecimal seckillLimit;

    private Integer seckillSort;

    private Long startTime;

    private Long endTime;

    private String randomCode;

    private SkuInfoEntity skuInfo;
}
