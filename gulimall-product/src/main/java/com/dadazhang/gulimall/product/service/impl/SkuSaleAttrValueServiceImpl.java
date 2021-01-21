package com.dadazhang.gulimall.product.service.impl;

import com.dadazhang.gulimall.product.vo.SkuItemVo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.product.dao.SkuSaleAttrValueDao;
import com.dadazhang.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.dadazhang.gulimall.product.service.SkuSaleAttrValueService;


@Service("skuSaleAttrValueService")
public class SkuSaleAttrValueServiceImpl extends ServiceImpl<SkuSaleAttrValueDao, SkuSaleAttrValueEntity> implements SkuSaleAttrValueService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuSaleAttrValueEntity> page = this.page(
                new Query<SkuSaleAttrValueEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<SkuItemVo.SkuItemSaleAttrVo> skuSaleAttrBySpuId(Long spuId) {
        return this.baseMapper.skuSaleAttrBySpuId(spuId);
    }

    @Override
    public List<String> getSkuSaleAttrBySkuId(Long skuId) {

        return this.baseMapper.skuSaleAttrListBySkuId(skuId);
    }

}