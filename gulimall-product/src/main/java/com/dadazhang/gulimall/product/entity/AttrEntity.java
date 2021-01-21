package com.dadazhang.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 商品属性
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@Data
@TableName("pms_attr")
public class AttrEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long attrId;

	private String attrName;

	private Integer searchType;

	private String icon;

	private String valueSelect;

	private Integer attrType;

	private Long enable;

	private Long catelogId;

	private Integer showDesc;

	private Integer valueType;

}
