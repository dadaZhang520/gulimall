package com.dadazhang.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 商品会员价格
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:22:10
 */
@Data
@TableName("sms_member_price")
public class MemberPriceEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Long skuId;

	private Long memberLevelId;

	private String memberLevelName;

	private BigDecimal memberPrice;

	private Integer addOther;

}
