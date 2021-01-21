package com.dadazhang.gulimall.coupon.service.impl;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.coupon.dao.SeckillSkuRelationDao;
import com.dadazhang.gulimall.coupon.entity.SeckillSkuRelationEntity;
import com.dadazhang.gulimall.coupon.service.SeckillSkuRelationService;
import org.springframework.util.StringUtils;


@Service("seckillSkuRelationService")
public class SeckillSkuRelationServiceImpl extends ServiceImpl<SeckillSkuRelationDao, SeckillSkuRelationEntity> implements SeckillSkuRelationService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        QueryWrapper<SeckillSkuRelationEntity> wrapper = new QueryWrapper<>();

        String promotionSessionId = (String) params.get("promotionSessionId");
        String key = (String) params.get("key");

        if (!StringUtils.isEmpty(key)) {
            wrapper.and(obj -> {
                obj.eq("sku_id", key).or().eq("seckill_price", key);
            });
        }

        if (!StringUtils.isEmpty(promotionSessionId)) {
            wrapper.eq("promotion_session_id", promotionSessionId);
        }


        IPage<SeckillSkuRelationEntity> page = this.page(
                new Query<SeckillSkuRelationEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);
    }

    @Override
    public List<SeckillSkuRelationEntity> listSeckillSkuRelationBySessionId(Long sessionId) {
        return list(new QueryWrapper<SeckillSkuRelationEntity>().eq("promotion_session_id", sessionId));
    }


}