package com.dadazhang.gulimall.member.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class OrderVo {

    private int id;
    private int memberId;
    private String orderSn;
    private Date createTime;
    private String memberUsername;
    private int totalAmount;
    private int payAmount;
    private int freightAmount;
    private int promotionAmount;
    private int integrationAmount;
    private int couponAmount;
    private int status;
    private int autoConfirmDay;
    private int integration;
    private int growth;
    private String receiverName;
    private String receiverPhone;
    private String receiverPostCode;
    private String receiverProvince;
    private String receiverCity;
    private String receiverRegion;
    private String receiverDetailAddress;
    private int deleteStatus;
    private List<OrderItems> orderItems;

}