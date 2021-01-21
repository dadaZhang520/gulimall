package com.dadazhang.gulimall.seckill.service.impl;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.dadazhang.common.constant.MQConstant;
import com.dadazhang.common.to.mq.SeckillOrderTo;
import com.dadazhang.common.utils.R;
import com.dadazhang.common.vo.MemberVo;
import com.dadazhang.gulimall.seckill.constant.SeckillConstant;
import com.dadazhang.gulimall.seckill.feign.CouponFeignService;
import com.dadazhang.gulimall.seckill.feign.ProductFeignService;
import com.dadazhang.gulimall.seckill.interceptor.LoginUserInterceptor;
import com.dadazhang.gulimall.seckill.service.SeckillService;
import com.dadazhang.gulimall.seckill.service.fallback.SeckillMethodFallback;
import com.dadazhang.gulimall.seckill.to.SeckillRedisTo;
import com.dadazhang.gulimall.seckill.vo.SeckillSessionVo;
import com.dadazhang.gulimall.seckill.vo.SeckillSkuRelationVo;
import com.dadazhang.gulimall.seckill.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     * 上架三天秒杀活动的商品
     */
    @Override
    public void uploadThreeDaysSeckillSku() {
        //1）、远程调用获取三天内的秒杀信息
        R r = couponFeignService.threeDaysSeckillSku();
        if (r.getCode() == 0) {
            List<SeckillSessionVo> sessionVos = r.getData(new TypeReference<List<SeckillSessionVo>>() {
            });
            //2）、如果存在秒杀商品，对商品进行缓存。
            //2.1）、缓存秒杀的场次信息
            redisSeckillInfos(sessionVos);
            //2.2）、缓存秒杀的商品信息
            redisSkuSeckillInfos(sessionVos);
        }
    }

    /**
     * 获取符合当前时间的秒杀商品
     */
    @SentinelResource(value = "currentSeckillSkuResource", fallback = "blockHandlerGetCurrentSeckillSku", fallbackClass = SeckillMethodFallback.class)
    @Override
    public List<SeckillRedisTo> currentSeckillSku() {
        List<SeckillRedisTo> seckillRedisTos = new ArrayList<>();
        try (Entry entry = SphU.entry("seckillSkus")) {
            //1）、获取当前的时间
            long now = new Date().getTime();
            //2）、比较符合当前时间的秒杀场次
            //2.1）、获取全部秒杀场次
            Set<String> keys = stringRedisTemplate.keys(SeckillConstant.SESSION_CACHE_PREFIX + "*");
            //2.2）、筛选符合的秒杀场次
            for (String key : keys) {
                String replace = key.replace(SeckillConstant.SESSION_CACHE_PREFIX, "");
                String[] spt = replace.split("_");
                long startTime = Long.parseLong(spt[0]);
                long endTime = Long.parseLong(spt[1]);
                //2.3）、筛选成功的秒杀场次
                if (now >= startTime && now <= endTime) {
                    //2.4）、获取筛选成功的秒杀商品skuId
                    List<String> skuIds = stringRedisTemplate.opsForList().
                            range(SeckillConstant.SESSION_CACHE_PREFIX + startTime + "_" + endTime, -100, 100);

                    //2.5）、获取当前秒杀场次下的所有商品的信息
                    BoundHashOperations<String, String, String> ops =
                            stringRedisTemplate.boundHashOps(SeckillConstant.SKUSECKILL_CACHE_PREFIX);
                    //2.6）、所有秒杀商品信息的
                    List<String> skuInfos = ops.multiGet(skuIds);
                    //2.7）、将获取到的所有秒杀商品封装成 SeckillRedisTo 返回
                    List<SeckillRedisTo> seckillRedisToList = skuInfos.stream().
                            map(info -> JSON.parseObject(info, SeckillRedisTo.class)).collect(Collectors.toList());
                    seckillRedisTos.addAll(seckillRedisToList);
                }
            }
        } catch (BlockException e) {
            log.error("方法被限流:{}", e.getMessage());
        }
        return seckillRedisTos;
    }

    /**
     * 获取当前sku的秒杀预告信息
     */
    @Override
    public SeckillRedisTo getSeckillBySkuId(Long skuId) {
        //1）、获取所有的缓存的sku信息
        BoundHashOperations<String, String, String> ops =
                stringRedisTemplate.boundHashOps(SeckillConstant.SKUSECKILL_CACHE_PREFIX);
        Set<String> keys = ops.keys();
        if (keys != null && keys.size() > 0) {
            //2）、匹配当前的sku是否存在
            String reg = "\\d_" + skuId;
            for (String key : keys) {
                //2.1）、匹配存在
                if (Pattern.matches(reg, key)) {
                    String json = ops.get(key);
                    return JSON.parseObject(json, SeckillRedisTo.class);
                }
            }
        }
        return null;
    }

    /**
     * 开始秒杀
     */
    @Override
    public String secKill(Long sessionId, Long skuId, String code, Integer shopNum) throws InterruptedException {

        MemberVo memberVo = LoginUserInterceptor.loginUser.get();

        //1）、校验商品的合法性
        String key = sessionId + "_" + skuId;
        //1.1）、在缓存中获取当前商品是否存在
        BoundHashOperations<String, String, String> ops =
                stringRedisTemplate.boundHashOps(SeckillConstant.SKUSECKILL_CACHE_PREFIX);
        String json = ops.get(key);
        //存在就进行校验
        if (!StringUtils.isEmpty(json)) {
            SeckillRedisTo seckillRedisTo = JSON.parseObject(json, SeckillRedisTo.class);
            //1.2）、校验时间秒杀时间是否符合
            long startTime = seckillRedisTo.getStartTime();
            long endTime = seckillRedisTo.getEndTime();
            long now = new Date().getTime();
            if (now >= startTime && now <= endTime) {
                //1.3）、校验秒杀的场次id和商品id、随机码是否一致
                if (seckillRedisTo.getSkuId().equals(skuId)
                        && seckillRedisTo.getPromotionSessionId().equals(sessionId)
                        && seckillRedisTo.getRandomCode().equals(code)) {
                    //1.4）、校验购买的数量是否在限制数量内
                    if (seckillRedisTo.getSeckillLimit().intValue() >= shopNum) {
                        //1.5）、进行幂等性处理，判断当前用户是否购买过此商品
                        //使用redis的占坑方法，如果当前用户购买过，占坑就会失败
                        String pit = memberVo.getId() + "_" + key;
                        Boolean aBoolean = stringRedisTemplate.opsForValue().
                                setIfAbsent(pit, shopNum.toString(), endTime - now, TimeUnit.MILLISECONDS);
                        //1.6）、如果占坑成功就是第一次购买
                        if (aBoolean) {
                            //1.7）、获取信号量
                            RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + code);
                            //使用快速尝试获取信号量方法，使用acquire来获取是阻塞的。
                            boolean b = semaphore.tryAcquire(shopNum, 100, TimeUnit.MILLISECONDS);
                            //信号量获取成功，秒杀商品就成功
                            if (b) {
                                //快速下单。发送消息，构建SeckillOrderTo发送MQ
                                //1.8）、生成秒杀订单号
                                String orderSn = IdWorker.getTimeId();
                                SeckillOrderTo seckillOrderTo = new SeckillOrderTo();
                                seckillOrderTo.setOrderSn(orderSn);
                                seckillOrderTo.setSessionId(seckillRedisTo.getPromotionSessionId());
                                seckillOrderTo.setMemberId(memberVo.getId());
                                seckillOrderTo.setSkuId(seckillRedisTo.getSkuId());
                                seckillOrderTo.setPrice(seckillRedisTo.getSeckillPrice());
                                seckillOrderTo.setNum(shopNum);
                                //1.9）、发送消息
                                rabbitTemplate.convertAndSend(MQConstant.RABBITMQ_ORDER_EXCHANGE,
                                        MQConstant.RABBITMQ_ORDER_SECKILLROUTING_KEY, seckillOrderTo);
                                return orderSn;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 缓存秒杀的场次信息
     * 保存的redis的类型是List  key:SESSION_CACHE_PREFIX + start_end value（list）的skuIds: [1,2,3,4]
     */
    private void redisSkuSeckillInfos(List<SeckillSessionVo> sessionVos) {
        if (sessionVos != null && sessionVos.size() > 0) {
            sessionVos.forEach(session -> {
                //1）、设置缓存 key
                long start = session.getStartTime().getTime();
                long end = session.getEndTime().getTime();
                String key = SeckillConstant.SESSION_CACHE_PREFIX + start + "_" + end;
                List<String> value = null;

                //2）、判断已存在的秒杀场次是否有新增的秒杀商品，有就添加
                if (stringRedisTemplate.hasKey(key)) {
                    //2.1）、获取存在的秒杀商品id
                    BoundListOperations<String, String> ops = stringRedisTemplate.boundListOps(key);
                    List<String> range = ops.range(0, ops.size());
                    if (range != null && range.size() > 0) {
                        value = session.getSeckillSkuRelations().stream().
                                map(item -> session.getId() + "_" + item.getSkuId())
                                .filter(item -> !range.contains(item))
                                .collect(Collectors.toList());
                    }
                } else {
                    value = session.getSeckillSkuRelations().stream().
                            map(item -> session.getId() + "_" + item.getSkuId())
                            .collect(Collectors.toList());
                }
                //注意：使用StringRedisTemplate时，传入的类型和返回的类型都是String类型
                //3）、添加到缓存中
                if (value != null && value.size() > 0) {
                    //保存
                    stringRedisTemplate.opsForList().leftPushAll(key, value);
                    //设置redis过期时间，为秒杀场次的结束时间。
                    stringRedisTemplate.expireAt(key, session.getEndTime());
                }
            });
        }
    }

    /**
     * 缓存秒杀的商品信息
     * 保存的redis类型是Hash  redis_key: seckill:stock   map_key: sessionid_skuid   value: sku的详情信息
     */
    private void redisSeckillInfos(List<SeckillSessionVo> sessionVos) {
        if (sessionVos != null && sessionVos.size() > 0) {
            //绑定一个redis的hash操作
            BoundHashOperations<String, Object, Object> ops =
                    stringRedisTemplate.boundHashOps(SeckillConstant.SKUSECKILL_CACHE_PREFIX);
            for (SeckillSessionVo session : sessionVos) { //1）、缓存sku详情信息
                List<SeckillSkuRelationVo> seckillSkuRelations = session.getSeckillSkuRelations();
                if (seckillSkuRelations != null && seckillSkuRelations.size() > 0) {
                    for (SeckillSkuRelationVo seckillSkuRelation : seckillSkuRelations) {
                        //map的key
                        String key = seckillSkuRelation.getPromotionSessionId() + "_" + seckillSkuRelation.getSkuId();
                        if (ops.hasKey(key)) {
                            continue;
                        }
                        //2）、构建SeckillRedisTo封装存储对象
                        SeckillRedisTo seckillRedisTo = new SeckillRedisTo();
                        BeanUtils.copyProperties(seckillSkuRelation, seckillRedisTo);
                        //3）、调用远程服务，获取skuInfo的内容
                        R r = productFeignService.info(seckillSkuRelation.getSkuId());
                        if (r.getCode() == 0) {
                            SkuInfoVo skuInfoVo = r.getData(new TypeReference<SkuInfoVo>() {
                            });
                            if (skuInfoVo != null) {
                                seckillRedisTo.setSkuInfo(skuInfoVo);
                            }
                        }

                        //秒杀开始，结束时间
                        seckillRedisTo.setStartTime(session.getStartTime().getTime());
                        seckillRedisTo.setEndTime(session.getEndTime().getTime());
                        //4）、设置随机码，当我们暴露出秒杀接口服务时，请求必须带上随机码，才能进行秒杀
                        String token = UUID.randomUUID().toString().replace("-", "");
                        seckillRedisTo.setRandomCode(token);
                        //5）、引入分布式的信号量，作为库存
                        RSemaphore semaphore = redissonClient.getSemaphore(SeckillConstant.SKU_STOCK_SEMAPHORE + token);
                        //5.1）、设置信号量的大小
                        semaphore.trySetPermits(seckillSkuRelation.getSeckillCount().intValue());
                        //5.2）、设置信号量和秒杀商品信息的过期时间
                        long expireSecond = session.getEndTime().
                                toInstant().
                                atZone(ZoneId.systemDefault()).
                                plusHours(1).toEpochSecond() * 1000;

                        semaphore.expireAt(new Date(expireSecond));
                        //保存的内容为json内容
                        String json = JSON.toJSONString(seckillRedisTo);
                        //保存秒杀商品
                        ops.put(key, json);
                        //设置缓存过期时间
                        ops.expireAt(new Date(expireSecond));
                    }
                }
            }
        }
    }
}
