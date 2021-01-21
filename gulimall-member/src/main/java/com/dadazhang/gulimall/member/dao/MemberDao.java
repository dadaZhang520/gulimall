package com.dadazhang.gulimall.member.dao;

import com.dadazhang.gulimall.member.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:34:49
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
