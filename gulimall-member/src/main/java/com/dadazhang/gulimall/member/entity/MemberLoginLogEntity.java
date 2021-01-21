package com.dadazhang.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 会员登录记录
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:34:49
 */
@Data
@TableName("ums_member_login_log")
public class MemberLoginLogEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Long memberId;

	private Date createTime;

	private String ip;

	private String city;

	private Integer loginType;

}
