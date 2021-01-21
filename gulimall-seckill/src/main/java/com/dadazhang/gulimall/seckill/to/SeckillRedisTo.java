package com.dadazhang.gulimall.seckill.to;

import com.dadazhang.gulimall.seckill.vo.SkuInfoVo;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SeckillRedisTo {

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

    private SkuInfoVo skuInfo;
}
