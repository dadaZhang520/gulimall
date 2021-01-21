package com.dadazhang.gulimall.seckill.service;

import com.dadazhang.gulimall.seckill.to.SeckillRedisTo;

import java.util.List;

public interface SeckillService {

    void uploadThreeDaysSeckillSku();

    List<SeckillRedisTo> currentSeckillSku();

    SeckillRedisTo getSeckillBySkuId(Long skuId);

    String secKill(Long sessionId, Long skuId, String code, Integer shopNum) throws InterruptedException;
}
