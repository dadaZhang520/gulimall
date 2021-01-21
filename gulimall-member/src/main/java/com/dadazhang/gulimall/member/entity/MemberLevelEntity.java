package com.dadazhang.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会员等级
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:34:49
 */
@Data
@TableName("ums_member_level")
public class MemberLevelEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private String name;

	private Integer growthPoint;

	private Integer defaultStatus;

	private BigDecimal freeFreightPoint;

	private Integer commentGrowthPoint;

	private Integer priviledgeFreeFreight;

	private Integer priviledgeMemberPrice;

	private Integer priviledgeBirthday;

	private String note;

}
