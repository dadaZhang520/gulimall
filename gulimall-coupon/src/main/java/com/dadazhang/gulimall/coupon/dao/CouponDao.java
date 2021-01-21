package com.dadazhang.gulimall.coupon.dao;

import com.dadazhang.gulimall.coupon.entity.CouponEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 优惠券信息
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:22:10
 */
@Mapper
public interface CouponDao extends BaseMapper<CouponEntity> {
	
}
