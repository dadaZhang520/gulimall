package com.dadazhang.gulimall.member.vo;

import lombok.Data;

@Data
public class OrderItems {

    private int id;
    private String orderSn;
    private int spuId;
    private String spuName;
    private String spuPic;
    private String spuBrand;
    private int categoryId;
    private int skuId;
    private String skuName;
    private String skuPic;
    private int skuPrice;
    private int skuQuantity;
    private String skuAttrsVals;
    private int promotionAmount;
    private int couponAmount;
    private int integrationAmount;
    private int realAmount;
    private int giftIntegration;
    private int giftGrowth;

}