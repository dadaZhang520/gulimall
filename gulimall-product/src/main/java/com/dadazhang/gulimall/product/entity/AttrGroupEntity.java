package com.dadazhang.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import lombok.Data;

/**
 * 属性分组
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@Data
@TableName("pms_attr_group")
public class AttrGroupEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long attrGroupId;

	private String attrGroupName;

	private Integer sort;

	private String descript;

	private String icon;

	private Long catelogId;

	@TableField(exist = false)
	private Long[] catelogPath;
}
