package com.dadazhang.gulimall.product.vo;

import com.dadazhang.gulimall.product.entity.SkuImagesEntity;
import com.dadazhang.gulimall.product.entity.SkuInfoEntity;
import com.dadazhang.gulimall.product.entity.SpuInfoDescEntity;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
public class SkuItemVo {

    //sku的基本信息
    private SkuInfoEntity skuInfo;
    //是否有库存
    private Boolean hasStock;
    //sku的图片信息
    private List<SkuImagesEntity> skuImages;
    //Spu下的所有sku的销售属性组合
    private List<SkuItemSaleAttrVo> skuSaleInfo;
    //spu的介绍
    private SpuInfoDescEntity desc;
    //spu的规格参数
    private List<SpuItemBaseAttrVo> groupInfo;
    //秒杀信息
    private SeckillRedisVo seckillRedis;

    @ToString
    @Data
    public static class SkuItemSaleAttrVo {
        private Long attrId;
        private String attrName;
        private List<AttrValueWithSkuIdVo> attrValueWithSkuIdVos;
    }

    @ToString
    @Data
    public static class SpuItemBaseAttrVo {
        private String groupName;
        private List<SpuBaseAttrVo> attrVos;
    }

    @ToString
    @Data
    public static class SpuBaseAttrVo {
        private String attrName;
        private String attrValue;
    }

    @Data
    public static class AttrValueWithSkuIdVo {
        private String attrValues;
        private String skuId;
    }
}
