package com.dadazhang.gulimall.product.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 商品评价
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-21 15:28:42
 */
@Data
@TableName("pms_spu_comment")
public class SpuCommentEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Long skuId;

	private Long spuId;

	private String spuName;

	private String memberNickName;

	private Integer star;

	private String memberIp;

	private Date createTime;

	private Integer showStatus;

	private String spuAttributes;

	private Integer likesCount;

	private Integer replyCount;

	private String resources;

	private String content;

	private String memberIcon;

	private Integer commentType;

}
