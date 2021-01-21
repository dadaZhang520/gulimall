package com.dadazhang.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 支付信息表
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:27:45
 */
@Data
@TableName("oms_payment_info")
public class PaymentInfoEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@TableId
	private Long id;

	private String orderSn;

	private Long orderId;

	private String alipayTradeNo;

	private BigDecimal totalAmount;

	private String subject;

	private String paymentStatus;

	private Date createTime;

	private Date confirmTime;

	private String callbackContent;

	private Date callbackTime;

}
