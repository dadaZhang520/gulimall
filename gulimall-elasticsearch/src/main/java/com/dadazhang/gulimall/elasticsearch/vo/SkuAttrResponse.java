package com.dadazhang.gulimall.elasticsearch.vo;

import lombok.Data;

@Data
public class SkuAttrResponse {

    private Long id;

    private Long skuId;

    private Long attrId;

    private String attrName;

    private String attrValue;

    private Integer attrSort;
}
