package com.dadazhang.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会员统计信息
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:34:49
 */
@Data
@TableName("ums_member_statistics_info")
public class MemberStatisticsInfoEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Long memberId;

	private BigDecimal consumeAmount;

	private BigDecimal couponAmount;

	private Integer orderCount;

	private Integer couponCount;

	private Integer commentCount;

	private Integer returnOrderCount;

	private Integer loginCount;

	private Integer attendCount;

	private Integer fansCount;

	private Integer collectProductCount;

	private Integer collectSubjectCount;

	private Integer collectCommentCount;

	private Integer inviteFriendCount;

}
