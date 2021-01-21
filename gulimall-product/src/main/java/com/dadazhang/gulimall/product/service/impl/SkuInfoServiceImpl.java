package com.dadazhang.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.product.entity.SkuImagesEntity;
import com.dadazhang.gulimall.product.entity.SpuInfoDescEntity;
import com.dadazhang.gulimall.product.feign.SeckillFeignService;
import com.dadazhang.gulimall.product.feign.WareFeignService;
import com.dadazhang.gulimall.product.service.*;
import com.dadazhang.gulimall.product.vo.SeckillRedisVo;
import com.dadazhang.gulimall.product.vo.SkuItemVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.product.dao.SkuInfoDao;
import com.dadazhang.gulimall.product.entity.SkuInfoEntity;


@Service("skuInfoService")
public class SkuInfoServiceImpl extends ServiceImpl<SkuInfoDao, SkuInfoEntity> implements SkuInfoService {

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    AttrGroupService attrGroupService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SeckillFeignService seckillFeignService;

    @Autowired
    ThreadPoolExecutor executor;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                new QueryWrapper<>()
        );

        return new PageUtils(page);
    }

    @Override
    public void saveSkuInfo(SkuInfoEntity skuInfoEntity) {
        this.baseMapper.insert(skuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SkuInfoEntity> wrapper = new QueryWrapper<>();

        //获取sku检索的名称
        String key = (String) params.get("key");
        //获取sku商品分类id
        String catelogId = (String) params.get("catelogId");
        //获取sku商品品牌id
        String brandId = (String) params.get("brandId");
        //获取sku的价格范围
        String min = (String) params.get("min");
        String max = (String) params.get("max");

        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("sku_id", key).or().like("sku_name", key);
            });
        }

        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        if (!StringUtils.isEmpty(min) && !StringUtils.isEmpty(max)) {
            try {
                BigDecimal minPrice = new BigDecimal(min);
                BigDecimal maxPrice = new BigDecimal(max);
                if (minPrice.compareTo(new BigDecimal("0")) > 0) {
                    wrapper.ge("price", minPrice);
                }
                if (maxPrice.compareTo(new BigDecimal("0")) > 0) {
                    wrapper.le("price", maxPrice);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        IPage<SkuInfoEntity> page = this.page(
                new Query<SkuInfoEntity>().getPage(params),
                wrapper
        );

        return new PageUtils(page);

    }

    @Override
    public SkuItemVo item(Long skuId) throws ExecutionException, InterruptedException {
        SkuItemVo skuItemVo = new SkuItemVo();

        //            Long spuId = skuInfo.getSpuId(); //获取SPU的id
        //            Long catalogId = skuInfo.getCatalogId(); //获取Catalog的id

        //1.）sku的基本信息
        CompletableFuture<SkuInfoEntity> infoFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfoEntity skuInfo = getById(skuId);
            skuItemVo.setSkuInfo(skuInfo);
            return skuInfo;
        }, executor);


        //2.）sku的图片信息
        CompletableFuture<Void> skuImgsFuture = infoFuture.thenRunAsync(() -> {
            List<SkuImagesEntity> skuImages = skuImagesService.list(new QueryWrapper<SkuImagesEntity>().eq("sku_id", skuId));
            skuItemVo.setSkuImages(skuImages);
        }, executor);


        //3.）获取当前Spu下的所有sku的销售属性组合
        CompletableFuture<Void> skuSaleAttrFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemVo.SkuItemSaleAttrVo> skuSaleAttrs = skuSaleAttrValueService.skuSaleAttrBySpuId(res.getSpuId());
            skuItemVo.setSkuSaleInfo(skuSaleAttrs);
        }, executor);

        //4.）获取spu介绍
        CompletableFuture<Void> spuDescFuture = infoFuture.thenAcceptAsync((res) -> {
            SpuInfoDescEntity spuInfoDesc = spuInfoDescService.getById(res.getSpuId());
            skuItemVo.setDesc(spuInfoDesc);
        }, executor);


        //5.）获取spu规格参数
        CompletableFuture<Void> spuGroupInfoFuture = infoFuture.thenAcceptAsync((res) -> {
            List<SkuItemVo.SpuItemBaseAttrVo> groupInfo = attrGroupService.getAttrGroupBySpuId(res.getSpuId(), res.getCatalogId());
            skuItemVo.setGroupInfo(groupInfo);
        }, executor);

        //6.）查询是否有库存
        CompletableFuture<Void> skuStockFuture = infoFuture.thenRunAsync(() -> skuItemVo.setHasStock(wareFeignService.skuHasStockBySkuId(skuId)), executor);

        //7.）获取sku的秒杀信息
        CompletableFuture<Void> seckillInfoFuture = CompletableFuture.runAsync(() -> {
            R r = seckillFeignService.getSeckillBySkuId(skuId);
            skuItemVo.setSeckillRedis(r.getData(new TypeReference<SeckillRedisVo>(){}));
        }, executor);

        CompletableFuture.allOf(skuImgsFuture, skuSaleAttrFuture, spuDescFuture, spuGroupInfoFuture, skuStockFuture, seckillInfoFuture).get();

        return skuItemVo;
    }

    @Override
    public BigDecimal getPrice(Long skuId) {
        return this.getOne(new QueryWrapper<SkuInfoEntity>().eq("sku_id", skuId)).getPrice();
    }

}