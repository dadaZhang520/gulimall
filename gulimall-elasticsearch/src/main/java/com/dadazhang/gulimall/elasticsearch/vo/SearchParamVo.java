package com.dadazhang.gulimall.elasticsearch.vo;

import lombok.Data;

import java.util.List;

@Data
public class SearchParamVo {

    //搜索框查询条件
    private String keyword;

    //三级分类id查询条件
    private Long catalog3Id;

    /**
     * 排序条件
     * sort=saleCount_acs/desc
     * sort=skuPrice_acs/desc
     * sort=hotScore_asc/des
     */
    private String sort;

    /**
     * 过滤条件
     * hasStock(是否有库存),skuPrice（价格区间）,brandId（品牌id）,attrs(sku属性过滤)
     * hasStock=0/1
     * skuPrice=1_500/_500/500_
     */
    private Integer hasStock;
    private String skuPrice;
    private List<Long> brandId;
    private List<String> attr;
    private List<String> skuAttrs;

    //分页页码
    private Integer pageNum = 1;

    //原生路径参数
    private String queryString;
}
