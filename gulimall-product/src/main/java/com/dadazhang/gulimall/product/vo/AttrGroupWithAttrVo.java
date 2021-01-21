package com.dadazhang.gulimall.product.vo;

import com.dadazhang.gulimall.product.entity.AttrEntity;
import lombok.Data;

import java.util.List;

@Data
public class AttrGroupWithAttrVo {


    private Long attrGroupId;

    private String attrGroupName;

    private Integer sort;

    private String descript;

    private String icon;

    private Long catelogId;

    private List<AttrEntity> attrs;

}
