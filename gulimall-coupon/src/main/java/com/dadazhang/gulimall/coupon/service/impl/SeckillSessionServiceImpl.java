package com.dadazhang.gulimall.coupon.service.impl;

import com.dadazhang.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.dadazhang.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.coupon.dao.SeckillSessionDao;
import com.dadazhang.gulimall.coupon.entity.SeckillSessionEntity;
import com.dadazhang.gulimall.coupon.service.SeckillSessionService;


@Service("seckillSessionService")
public class SeckillSessionServiceImpl extends ServiceImpl<SeckillSessionDao, SeckillSessionEntity> implements SeckillSessionService {

    @Autowired
    SeckillSkuRelationService seckillSkuRelationService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SeckillSessionEntity> page = this.page(
                new Query<SeckillSessionEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSessionEntity> threeDaysSeckillSession() {

        //1）、计算三天的开始日期和结束日期
        String start = startTime();
        String end = endTime();
        //2）、查询三天的秒杀场次
        List<SeckillSessionEntity> sessionEntities = baseMapper.threeDaysSeckillSession(start, end);
        if (sessionEntities != null && sessionEntities.size() > 0) {
            //3）、通过秒杀场次查询出每个场次的秒杀商品
            return sessionEntities.stream().peek(session -> {
                List<SeckillSkuRelationEntity> seckillSkuRelationEntities =
                        seckillSkuRelationService.listSeckillSkuRelationBySessionId(session.getId());
                session.setSeckillSkuRelations(seckillSkuRelationEntities);
            }).collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 秒杀上架的开始时间
     */
    private String startTime() {
        //获取当前的日期
        LocalDate date = LocalDate.now();
        //获取最小时间
        LocalTime time = LocalTime.MIN;
        //组合成一个日期
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        //日期格式化
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 秒杀上架的结束时间
     */
    private String endTime() {
        //获取第三天的结束日期
        LocalDate date = LocalDate.now().plusDays(2);
        //获取第三天的最大时间
        LocalTime time = LocalTime.MAX;
        //组合成一个日期
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        //日期格式化
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}