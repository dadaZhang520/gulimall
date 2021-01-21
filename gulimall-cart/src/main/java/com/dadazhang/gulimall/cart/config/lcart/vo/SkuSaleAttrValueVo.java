package com.dadazhang.gulimall.cart.config.lcart.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * sku销售属性&值
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@Data
public class SkuSaleAttrValueVo implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;

	private Long skuId;

	private Long attrId;

	private String attrName;

	private String attrValue;

	private Integer attrSort;
	
}
