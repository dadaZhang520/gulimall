package com.dadazhang.gulimall.elasticsearch.vo;

import com.dadazhang.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SearchResultVo
{

    //查询到的所有商品
    private List<SkuEsModel> products;

    /**
     * 分页信息
     */
    private Integer pageNum;
    private Long totalCount;
    private Integer totalPage;

    private List<BrandVo> brands;
    private List<CatalogVo> catalogs;
    private List<AttrVo> attrs;
    private List<SkuAttrVo> skuAttrs;

    private List<Long> attrNavIds = new ArrayList<>();

    private List<Long> skuAttrNavIds = new ArrayList<>();

    private List<NavVo> navs;

    @Data
    public static class NavVo {
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo {
        private Long brandId;
        private String brandName;
        private String img;
    }

    @Data
    public static class CatalogVo {
        private Long catId;
        private String catName;
    }

    @Data
    public static class AttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }

    @Data
    public static class SkuAttrVo {
        private Long attrId;
        private String attrName;
        private List<String> attrValues;
    }
}
