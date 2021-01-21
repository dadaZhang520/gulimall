package com.dadazhang.gulimall.order.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class SpuInfoVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String spuName;

    private String spuDescription;

    private Long catalogId;

    private Long brandId;

    private BigDecimal weight;

    private Integer publishStatus;

    private Date createTime;

    private Date updateTime;

    @Data
    public static class SpuDescInfo {

        private Long spuId;

        private String decript;
    }

    @Data
    public static class BrandInfo{
        private Long brandId;

        private String name;

        private String logo;
    }
}
