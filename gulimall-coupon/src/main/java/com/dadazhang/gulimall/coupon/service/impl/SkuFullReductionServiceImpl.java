package com.dadazhang.gulimall.coupon.service.impl;

import com.dadazhang.common.to.MemberPrice;
import com.dadazhang.common.to.SkuReductionTo;
import com.dadazhang.gulimall.coupon.entity.MemberPriceEntity;
import com.dadazhang.gulimall.coupon.entity.SkuLadderEntity;
import com.dadazhang.gulimall.coupon.service.MemberPriceService;
import com.dadazhang.gulimall.coupon.service.SkuLadderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.coupon.dao.SkuFullReductionDao;
import com.dadazhang.gulimall.coupon.entity.SkuFullReductionEntity;
import com.dadazhang.gulimall.coupon.service.SkuFullReductionService;
import org.springframework.transaction.annotation.Transactional;


@Service("skuFullReductionService")
public class SkuFullReductionServiceImpl extends ServiceImpl<SkuFullReductionDao, SkuFullReductionEntity> implements SkuFullReductionService {

    @Autowired
    SkuLadderService skuLadderService;

    @Autowired
    MemberPriceService memberPriceService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuFullReductionEntity> page = this.page(
                new Query<SkuFullReductionEntity>().getPage(params),
                new QueryWrapper<SkuFullReductionEntity>()
        );

        return new PageUtils(page);
    }

    @Transactional
    @Override
    public void saveSkuReduction(SkuReductionTo skuReductionTo) {
        //1）保存sku的满减优惠信息
        if (skuReductionTo.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
            SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
            skuFullReductionEntity.setSkuId(skuReductionTo.getSkuId());
            skuFullReductionEntity.setFullPrice(skuReductionTo.getFullPrice());
            skuFullReductionEntity.setReducePrice(skuReductionTo.getReducePrice());
            skuFullReductionEntity.setAddOther(skuReductionTo.getPriceStatus());

            this.baseMapper.insert(skuFullReductionEntity);
        }

        //2）保存sku的阶段优惠信息
        if (skuReductionTo.getFullCount() > 0) {
            SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
            skuLadderEntity.setSkuId(skuReductionTo.getSkuId());
            skuLadderEntity.setFullCount(skuReductionTo.getFullCount());
            skuLadderEntity.setDiscount(skuReductionTo.getDiscount());
            skuLadderEntity.setAddOther(skuReductionTo.getCountStatus());
            //TODO: 设置保存折后价格  skuLadderEntity.setPrice();

            skuLadderService.saveSkuLadder(skuLadderEntity);
        }

        //3）保存sku的会员优惠信息
        List<MemberPrice> memberPrice = skuReductionTo.getMemberPrice();
        if (memberPrice != null && memberPrice.size() > 0) {
            List<MemberPriceEntity> memberPriceEntities = memberPrice.stream()
                    .filter(price -> price.getPrice().compareTo(new BigDecimal("0")) > 0)
                    .map(price -> {
                        MemberPriceEntity memberPriceEntity = new MemberPriceEntity();
                        memberPriceEntity.setSkuId(skuReductionTo.getSkuId());
                        memberPriceEntity.setMemberLevelId(price.getId());
                        memberPriceEntity.setMemberLevelName(price.getName());
                        memberPriceEntity.setMemberPrice(price.getPrice());
                        memberPriceEntity.setAddOther(1);
                        return memberPriceEntity;
                    }).collect(Collectors.toList());
            memberPriceService.saveBatch(memberPriceEntities);
        }
    }

}