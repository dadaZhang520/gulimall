package com.dadazhang.gulimall.order.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 订单配置信息
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:27:45
 */
@Data
@TableName("oms_order_setting")
public class OrderSettingEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Integer flashOrderOvertime;

	private Integer normalOrderOvertime;

	private Integer confirmOvertime;

	private Integer finishOvertime;

	private Integer commentOvertime;

	private Integer memberLevel;

}
