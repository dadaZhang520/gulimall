package com.dadazhang.gulimall.cart.config.lcart.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * sku信息
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@Data
public class SkuInfoVo implements Serializable {
	private static final long serialVersionUID = 1L;


	private Long skuId;

	private Long spuId;

	private String skuName;

	private String skuDesc;

	private Long catalogId;

	private Long brandId;

	private String skuDefaultImg;

	private String skuTitle;

	private String skuSubtitle;

	private BigDecimal price;

	private Long saleCount;

}
