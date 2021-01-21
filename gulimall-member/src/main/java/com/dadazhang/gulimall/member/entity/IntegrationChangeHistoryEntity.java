package com.dadazhang.gulimall.member.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 积分变化历史记录
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:34:49
 */
@Data
@TableName("ums_integration_change_history")
public class IntegrationChangeHistoryEntity implements Serializable {
	private static final long serialVersionUID = 1L;


	@TableId
	private Long id;

	private Long memberId;

	private Date createTime;

	private Integer changeCount;

	private String note;

	private Integer sourceTyoe;

}
