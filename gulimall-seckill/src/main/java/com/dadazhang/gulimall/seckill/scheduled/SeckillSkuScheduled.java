package com.dadazhang.gulimall.seckill.scheduled;

import com.dadazhang.gulimall.seckill.constant.SeckillConstant;
import com.dadazhang.gulimall.seckill.service.SeckillService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class SeckillSkuScheduled {

    @Autowired
    SeckillService seckillService;

    @Autowired
    RedissonClient redissonClient;

    /**
     * 每天凌晨三点扫描三天后的秒杀商品进行上架
     */
    //TODO: 幂等性处理，上架成功后就不需要重新上架
    @Scheduled(cron = "* * 3 * * ?")
    public void uploadThreeDaysSeckillSku() {
        RLock lock = redissonClient.getLock(SeckillConstant.SKU_UPLOAD_LOCK);
        lock.lock(10, TimeUnit.SECONDS);
        try {
            System.out.println("开始执行定时任务。。。。。。。");
            seckillService.uploadThreeDaysSeckillSku();
        } finally {
            lock.unlock();
        }
    }
}
