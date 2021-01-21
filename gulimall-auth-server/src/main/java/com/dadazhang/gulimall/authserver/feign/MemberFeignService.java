package com.dadazhang.gulimall.authserver.feign;

import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.authserver.vo.SocialUser;
import com.dadazhang.gulimall.authserver.vo.UserLoginVo;
import com.dadazhang.gulimall.authserver.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping("member/member/register")
    R register(@RequestBody UserRegisterVo userRegisterVo);

    @PostMapping("member/member/login")
    R login(@RequestBody UserLoginVo userLoginVo);

    @PostMapping("member/member/social/login")
    public R socialLogin(@RequestBody SocialUser socialUser);
}
