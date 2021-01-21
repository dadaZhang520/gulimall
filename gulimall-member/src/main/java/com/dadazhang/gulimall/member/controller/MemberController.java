package com.dadazhang.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.gulimall.member.exception.UserNameExistException;
import com.dadazhang.gulimall.member.exception.UserPhoneExistException;
import com.dadazhang.gulimall.member.feign.CouponFeignService;
import com.dadazhang.gulimall.member.vo.SocialUser;
import com.dadazhang.gulimall.member.vo.UserLoginVo;
import com.dadazhang.gulimall.member.vo.UserRegisterVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.dadazhang.gulimall.member.entity.MemberEntity;
import com.dadazhang.gulimall.member.service.MemberService;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.R;


/**
 * 会员
 *
 * @author zhangjiakun
 * @email zhangjiakun@gmail.com
 * @date 2020-08-20 13:34:49
 */
@RestController
@RequestMapping("member/member")
public class MemberController {
    @Autowired
    MemberService memberService;

    @Autowired
    CouponFeignService couponFeignService;

    @GetMapping("/coupons")
    public R test() {
        MemberEntity memberEntity = new MemberEntity();
        memberEntity.setNickname("张三");
        R memberCoupons = couponFeignService.memberCoupons();
        return R.ok().put("member", memberEntity).put("coupons", memberCoupons.get("coupons"));
    }

    /**
     * 社交登录
     */
    @PostMapping("/social/login")
    public R socialLogin(@RequestBody SocialUser socialUser) {

        MemberEntity memberEntity = memberService.socialLogin(socialUser);

        return R.ok().setData(memberEntity);
    }


    /**
     * 会员登录
     */
    @PostMapping("/login")
    public R login(@RequestBody UserLoginVo userLoginVo) {

        MemberEntity memberEntity = memberService.login(userLoginVo);

        return R.ok().setData(memberEntity);
    }

    /**
     * 会员注册
     */
    @PostMapping("/register")
    public R register(@RequestBody UserRegisterVo userRegisterVo) {

        memberService.register(userRegisterVo);

        return R.ok();
    }

    /**
     * 列表
     */
    @GetMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
