package com.dadazhang.gulimall.product.service.impl;

import com.alibaba.fastjson.TypeReference;
import com.dadazhang.common.to.SkuHasStockTo;
import com.dadazhang.common.to.SkuReductionTo;
import com.dadazhang.common.to.SpuBoundTo;
import com.dadazhang.common.to.es.SkuEsModel;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.product.constant.ProductConstant;
import com.dadazhang.gulimall.product.entity.*;
import com.dadazhang.gulimall.product.feign.CouponFeignService;
import com.dadazhang.gulimall.product.feign.SearchFeignService;
import com.dadazhang.gulimall.product.feign.WareFeignService;
import com.dadazhang.gulimall.product.service.*;
import com.dadazhang.gulimall.product.vo.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.product.dao.SpuInfoDao;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {

    @Autowired
    SpuInfoDescService spuInfoDescService;

    @Autowired
    SpuImagesService spuImagesService;

    @Autowired
    ProductAttrValueService productAttrValueService;

    @Autowired
    AttrService attrService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    SkuImagesService skuImagesService;

    @Autowired
    SkuSaleAttrValueService skuSaleAttrValueService;

    @Autowired
    CouponFeignService couponFeignService;

    @Autowired
    BrandService brandService;

    @Autowired
    CategoryService categoryService;

    @Autowired
    WareFeignService wareFeignService;

    @Autowired
    SearchFeignService searchFeignService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageUtils(page);
    }

    @GlobalTransactional
    @Transactional
    @Override
    public void saveSpuInfo(SpuSaveVo vo) {

        //1)保存spu基本信息 pms_spu_info
        SpuInfoEntity spuInfoEntity = new SpuInfoEntity();
        BeanUtils.copyProperties(vo, spuInfoEntity);
        spuInfoEntity.setCreateTime(new Date());
        spuInfoEntity.setUpdateTime(new Date());
        this.saveBaseInfo(spuInfoEntity);

        //2）保存spu的描述图片 pms_spu_info_desc
        SpuInfoDescEntity spuInfoDescEntity = new SpuInfoDescEntity();
        spuInfoDescEntity.setSpuId(spuInfoEntity.getId());
        spuInfoDescEntity.setDecript(String.join(",", vo.getDecript()));
        spuInfoDescService.saveSpuInfoDesc(spuInfoDescEntity);

        //3）保存spu的图片集  pms_spu_images
        spuImagesService.saveSpuImage(spuInfoEntity.getId(), vo.getImages());

        //4）保存spu的规格参数 pms_product_attr_value
        List<BaseAttrs> baseAttrs = vo.getBaseAttrs();
        if (baseAttrs != null && baseAttrs.size() > 0) {
            List<ProductAttrValueEntity> productAttrValueEntities = baseAttrs.stream().map(attr -> {
                ProductAttrValueEntity productAttrValueEntity = new ProductAttrValueEntity();
                productAttrValueEntity.setAttrValue(attr.getAttrValues());
                productAttrValueEntity.setQuickShow(attr.getShowDesc());
                productAttrValueEntity.setAttrId(attr.getAttrId());
                productAttrValueEntity.setSpuId(spuInfoEntity.getId());
                AttrEntity attrEntity = attrService.getById(attr.getAttrId());
                productAttrValueEntity.setAttrName(attrEntity.getAttrName());
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            productAttrValueService.saveProductAttrValue(productAttrValueEntities);
        }

        //5）保存sku的信息
        List<Skus> skus = vo.getSkus();
        if (skus != null && skus.size() > 0) {
            for (Skus sku : skus) {//获取sku默认图片
                String defaultImgUrl = "";
                for (Images image : sku.getImages()) {
                    if (image.getDefaultImg() == 1) {
                        defaultImgUrl = image.getImgUrl();
                    }
                }

                //保存sku的基本信息
                SkuInfoEntity skuInfoEntity = new SkuInfoEntity();
                BeanUtils.copyProperties(sku, skuInfoEntity);
                skuInfoEntity.setSpuId(spuInfoEntity.getId());
                skuInfoEntity.setBrandId(spuInfoEntity.getBrandId());
                skuInfoEntity.setCatalogId(spuInfoEntity.getCatalogId());
                skuInfoEntity.setSaleCount(0L);
                skuInfoEntity.setSkuDefaultImg(defaultImgUrl);
                //保存
                skuInfoService.saveSkuInfo(skuInfoEntity);

                //保存后获取sku的id
                Long skuId = skuInfoEntity.getSkuId();

                //保存sku的图片信息
                List<Images> images = sku.getImages();
                if (images != null && images.size() > 0) {
                    List<SkuImagesEntity> skuImagesEntities = images.stream()
                            .filter(image -> !StringUtils.isEmpty(image.getImgUrl()))
                            .map(image -> {
                                SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                                skuImagesEntity.setImgUrl(image.getImgUrl());
                                skuImagesEntity.setDefaultImg(image.getDefaultImg());
                                skuImagesEntity.setSkuId(skuId);

                                return skuImagesEntity;
                            }).collect(Collectors.toList());
                    //保存
                    skuImagesService.saveBatch(skuImagesEntities);
                }

                //保存sku的销售属性的信息
                List<Attr> attrs = sku.getAttr();
                if (attrs != null && attrs.size() > 0) {
                    List<SkuSaleAttrValueEntity> skuSaleAttrValueEntities = attrs.stream().map(attr -> {
                        SkuSaleAttrValueEntity skuSaleAttrValueEntity = new SkuSaleAttrValueEntity();
                        BeanUtils.copyProperties(attr, skuSaleAttrValueEntity);
                        skuSaleAttrValueEntity.setSkuId(skuId);
                        return skuSaleAttrValueEntity;
                    }).collect(Collectors.toList());
                    //保存
                    skuSaleAttrValueService.saveBatch(skuSaleAttrValueEntities);
                }

                //7）远程调用coupon服务，保sku的优惠信息
                //7.1）需要判断满几件的数量必须大于零，或者满多少的金额也要大于零才保存折扣信息
                if (sku.getFullCount() > 0 || sku.getFullPrice().compareTo(new BigDecimal("0")) > 0) {
                    SkuReductionTo skuReductionTo = new SkuReductionTo();
                    BeanUtils.copyProperties(sku, skuReductionTo);
                    skuReductionTo.setSkuId(skuId);
                    R r1 = couponFeignService.saveSkuReduction(skuReductionTo);
                }
            }
        }

        //6)远程调用coupon服务，对保存spu的积分信息
        //6.1)首先coupon服务必须开启了服务注册与发现功能
        //6.2）本服务必须开启Feign的远程调用功能
        //6.3）本服务必须指定Feign调用coupon服务的接口
        //6.4)本服务要向coupon服务传输数据，就需要在公共服务中构造一个TO模型（SpuBoundTo）
        SpuBoundTo spuBoundTo = new SpuBoundTo();
        BeanUtils.copyProperties(vo.getBounds(), spuBoundTo);
        spuBoundTo.setSpuId(spuInfoEntity.getId());
        R r = couponFeignService.saveSpuBound(spuBoundTo);

    }

    @Override
    public void saveBaseInfo(SpuInfoEntity spuInfoEntity) {
        this.baseMapper.insert(spuInfoEntity);
    }

    @Override
    public PageUtils queryPageByCondition(Map<String, Object> params) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();

        //获取检索的名称
        String key = (String) params.get("key");
        //获取商品分类id
        String catelogId = (String) params.get("catelogId");
        //获取商品品牌id
        String brandId = (String) params.get("brandId");
        //获取商品的显示状态status
        String status = (String) params.get("status");

        if (!StringUtils.isEmpty(key)) {
            wrapper.and(w -> {
                w.eq("id", key).or().like("spu_name", key);
            });
        }

        if (!StringUtils.isEmpty(catelogId) && !"0".equalsIgnoreCase(catelogId)) {
            wrapper.eq("catalog_id", catelogId);
        }

        if (!StringUtils.isEmpty(brandId) && !"0".equalsIgnoreCase(brandId)) {
            wrapper.eq("brand_id", brandId);
        }

        if (!StringUtils.isEmpty(status)) {
            wrapper.eq("publish_status", status);
        }

        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                wrapper
        );
        return new PageUtils(page);
    }

    @Override
    public void productUp(Long id) {
        //1）查询当前spu的sku信息
        List<SkuInfoEntity> skuInfoEntities = skuInfoService.list(new QueryWrapper<SkuInfoEntity>().eq("spu_id", id));

        //2）查询当前sku的可检索的基本属性信息
              /*
             *  private Long attrId;

                private String attrName;

                private String attrValue;
             */
        List<ProductAttrValueEntity> productAttrValueEntities = productAttrValueService.
                list(new QueryWrapper<ProductAttrValueEntity>().eq("spu_id", id));

        List<Long> attrIds = productAttrValueEntities.stream().
                map(ProductAttrValueEntity::getAttrId).collect(Collectors.toList());

        //过滤掉不能检索的属性
        Set<Long> setId = attrService.listByIds(attrIds).
                stream().filter(attrEntity -> attrEntity.getSearchType() == 1).
                map(AttrEntity::getAttrId).collect(Collectors.toSet());

        //查询可以索引的对象
        List<SkuEsModel.Attrs> attrs = productAttrValueEntities.stream().filter(
                productAttrValueEntity -> setId.contains(productAttrValueEntity.getAttrId())
        ).map(productAttrValueEntity -> {
            SkuEsModel.Attrs attr = new SkuEsModel.Attrs();
            BeanUtils.copyProperties(productAttrValueEntity, attr);
            return attr;
        }).collect(Collectors.toList());

        //3）获取库存状态
        Long[] skuIds = skuInfoEntities.stream().map(SkuInfoEntity::getSkuId).toArray(Long[]::new);

        //TODO 1、 hasStock 需要远程调用gulimall-ware的服务查询是否有库存
        Map<Long, Boolean> stockMap = new HashMap<>();

        try {
            // 获取库存的状态
            R r = wareFeignService.skuHasStockBySkuIds(skuIds);

            // 获取库存数据
            List<SkuHasStockTo> data = r.getData(new TypeReference<List<SkuHasStockTo>>() {
            });

            //给stockMap添加数据
            for (SkuHasStockTo skuHasStockTo : data) {
                stockMap.put(skuHasStockTo.getSkuId(), skuHasStockTo.getStock());
            }

        } catch (Exception e) {
            log.error("库存服务查询异常：", e);
        }

        //3）组装sku的信息
        List<SkuEsModel> skuEsModels = skuInfoEntities.stream().map(sku -> {
            SkuEsModel skuEsModel = new SkuEsModel();

            BeanUtils.copyProperties(sku, skuEsModel);

            //skuPrice,skuImg
            skuEsModel.setSkuPrice(sku.getPrice());
            skuEsModel.setSkuImg(sku.getSkuDefaultImg());

            //设置hasStock,hotScore
            skuEsModel.setHasStock(stockMap.get(sku.getSkuId()));

            // TODO 2、hotScore 热门评分，默认设置0
            skuEsModel.setHotScore(0L);

            //设置brandName,brandImg
            Long brandId = sku.getBrandId();

            BrandEntity brandEntity = brandService.getById(brandId);
            skuEsModel.setBrandName(brandEntity.getName());
            skuEsModel.setBrandImg(brandEntity.getLogo());

            //设置catalogName
            Long catalogId = sku.getCatalogId();

            CategoryEntity categoryEntity = categoryService.getById(catalogId);
            skuEsModel.setCatalogName(categoryEntity.getName());

            //todo 3、 添加可以索引的属性
            skuEsModel.setAttrs(attrs);

            //设置SkuAttr
            List<SkuEsModel.SkuAttrs> skuAttrsList = skuSaleAttrValueService.list(new QueryWrapper<SkuSaleAttrValueEntity>().eq("sku_id", sku.getSkuId()))
                    .stream().map(skuSaleAttrValueEntity -> {
                        SkuEsModel.SkuAttrs skuAttrs = new SkuEsModel.SkuAttrs();
                        skuAttrs.setAttrId(skuSaleAttrValueEntity.getAttrId());
                        skuAttrs.setAttrName(skuSaleAttrValueEntity.getAttrName());
                        skuAttrs.setAttrValue(skuSaleAttrValueEntity.getAttrValue());
                        return skuAttrs;
                    }).collect(Collectors.toList());

            skuEsModel.setSkuAttrs(skuAttrsList);

            return skuEsModel;

        }).collect(Collectors.toList());

        //todo 4、给search服务发送请求保存索引
        R r = searchFeignService.productStatusUp(skuEsModels);

        if (r.getCode() == 0) {
            SpuInfoEntity spuInfoEntity = new SpuInfoEntity();

            spuInfoEntity.setId(id);
            spuInfoEntity.setUpdateTime(new Date());
            spuInfoEntity.setPublishStatus(ProductConstant.SpuInfoEnum.UP_SPU.getCode());

            this.updateById(spuInfoEntity);
        } else {
            log.info("远程调用gulimall-search服务失败");
        }
    }

    @Override
    public SpuInfoEntity getInfoBySkuId(Long skuId) {

        return baseMapper.getInfoBySkuId(skuId);
    }


}