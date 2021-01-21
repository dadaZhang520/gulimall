package com.dadazhang.gulimall.ware.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 库存工作单
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:35:46
 */
@Data
@TableName("wms_ware_order_task_detail")
public class WareOrderTaskDetailEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	@TableId
	private Long id;

	private Long skuId;

	private String skuName;

	private Integer skuNum;

	private Long taskId;

	private Long wareId;

	private Integer lockStatus;

}
