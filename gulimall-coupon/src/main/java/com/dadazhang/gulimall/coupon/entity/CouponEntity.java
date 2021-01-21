package com.dadazhang.gulimall.coupon.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 优惠券信息
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:22:10
 */
@Data
@TableName("sms_coupon")
public class CouponEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Integer couponType;

	private String couponImg;

	private String couponName;

	private Integer num;

	private BigDecimal amount;

	private Integer perLimit;

	private BigDecimal minPoint;

	private Date startTime;

	private Date endTime;

	private Integer useType;

	private String note;

	private Integer publishCount;

	private Integer useCount;

	private Integer receiveCount;

	private Date enableStartTime;

	private Date enableEndTime;

	private String code;

	private Integer memberLevel;

	private Integer publish;

}
