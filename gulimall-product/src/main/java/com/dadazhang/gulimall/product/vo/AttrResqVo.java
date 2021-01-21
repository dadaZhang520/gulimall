package com.dadazhang.gulimall.product.vo;

import lombok.Data;

@Data
public class AttrResqVo extends AttrVo{

    private String catelogName;

    private String groupName;

    private Long[] catelogPath;
}
