package com.dadazhang.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 退款信息
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:27:45
 */
@Data
@TableName("oms_refund_info")
public class RefundInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Long orderReturnId;

	private BigDecimal refund;

	private String refundSn;

	private Integer refundStatus;

	private Integer refundChannel;

	private String refundContent;

}
