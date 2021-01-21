package com.dadazhang.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.to.SkuReductionTo;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:22:10
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTo skuReductionTo);
}

