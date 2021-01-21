package com.dadazhang.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.gulimall.member.entity.MemberEntity;
import com.dadazhang.gulimall.member.exception.UserNameExistException;
import com.dadazhang.gulimall.member.exception.UserPhoneExistException;
import com.dadazhang.gulimall.member.vo.SocialUser;
import com.dadazhang.gulimall.member.vo.UserLoginVo;
import com.dadazhang.gulimall.member.vo.UserRegisterVo;

import java.util.Map;

/**
 * 会员
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:34:49
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void register(UserRegisterVo userRegisterVo);

    void validUserNameUnique(String username) throws UserNameExistException;

    void validUserPhoneUnique(String phone) throws UserPhoneExistException;

    MemberEntity login(UserLoginVo userLoginVo);

    MemberEntity socialLogin(SocialUser socialUser);
}

