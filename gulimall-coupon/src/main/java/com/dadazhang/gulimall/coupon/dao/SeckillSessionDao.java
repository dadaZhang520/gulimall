package com.dadazhang.gulimall.coupon.dao;

import com.dadazhang.gulimall.coupon.entity.SeckillSessionEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 秒杀活动场次
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:22:10
 */
@Mapper
public interface SeckillSessionDao extends BaseMapper<SeckillSessionEntity> {

    List<SeckillSessionEntity> threeDaysSeckillSession(@Param("startTime") String startTime, @Param("endTime") String endTime);
}
