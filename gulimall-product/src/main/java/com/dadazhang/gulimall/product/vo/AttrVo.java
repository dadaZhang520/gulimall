package com.dadazhang.gulimall.product.vo;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class AttrVo {


    @TableId
    private Long attrId;

    private String attrName;

    private Integer searchType;

    private String icon;

    private String valueSelect;

    private Integer attrType;

    private Long enable;

    private Long catelogId;

    private Integer showDesc;

    private Integer valueType;

    private Long attrGroupId;

}
