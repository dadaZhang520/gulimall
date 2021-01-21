package com.dadazhang.gulimall.member.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dadazhang.common.utils.HttpUtils;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.member.entity.MemberLevelEntity;
import com.dadazhang.gulimall.member.exception.UserLoginNameException;
import com.dadazhang.gulimall.member.exception.UserLoginPasswordException;
import com.dadazhang.gulimall.member.exception.UserNameExistException;
import com.dadazhang.gulimall.member.exception.UserPhoneExistException;
import com.dadazhang.gulimall.member.service.MemberLevelService;
import com.dadazhang.gulimall.member.vo.SocialUser;
import com.dadazhang.gulimall.member.vo.UserLoginVo;
import com.dadazhang.gulimall.member.vo.UserRegisterVo;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dadazhang.common.utils.PageUtils;
import com.dadazhang.common.utils.Query;

import com.dadazhang.gulimall.member.dao.MemberDao;
import com.dadazhang.gulimall.member.entity.MemberEntity;
import com.dadazhang.gulimall.member.service.MemberService;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {

    @Autowired
    MemberLevelService memberLevelService;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * 注册会员
     *
     * @param userRegisterVo
     */
    @Override
    public void register(UserRegisterVo userRegisterVo) {
        MemberEntity memberEntity = new MemberEntity();

        //1.)查询默认会员等级
        MemberLevelEntity levelEntity = memberLevelService.getDefaultLevel();
        memberEntity.setLevelId(levelEntity.getId());

        //2.)检查用户名和手机号码的唯一性
        this.validUserNameUnique(userRegisterVo.getUsername());
        this.validUserPhoneUnique(userRegisterVo.getPhone());

        memberEntity.setUsername(userRegisterVo.getUsername());
        memberEntity.setMobile(userRegisterVo.getPhone());

        //3.)对密码进行加密存储
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encode = encoder.encode(userRegisterVo.getPassword());

        memberEntity.setPassword(encode);

        //4.）添加其他信息
        memberEntity.setCreateTime(new Date());
        memberEntity.setNickname(userRegisterVo.getUsername());

        //保存用户
        this.save(memberEntity);
    }

    @Override
    public void validUserNameUnique(String username) {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("username", username));
        if (count > 0) {
            throw new UserNameExistException();
        }
    }

    @Override
    public void validUserPhoneUnique(String phone) {
        Integer count = this.baseMapper.selectCount(new QueryWrapper<MemberEntity>().eq("mobile", phone));
        if (count > 0) {
            throw new UserPhoneExistException();
        }
    }

    @Override
    public MemberEntity login(UserLoginVo userLoginVo) {

        MemberEntity member = this.getOne(new QueryWrapper<MemberEntity>().eq("username", userLoginVo.getUsername()).or().eq("mobile", userLoginVo.getUsername()));

        if (member != null) {
            String sqlPassword = member.getPassword();
            if (!new BCryptPasswordEncoder().matches(userLoginVo.getPassword(), sqlPassword)) {
                throw new UserLoginPasswordException();
            }
        } else {
            throw new UserLoginNameException();
        }
        return member;
    }

    @Override
    public MemberEntity socialLogin(SocialUser socialUser) {

        //1。）判断数据库中是否存在社交登录的uid
        MemberEntity member = this.getOne(new QueryWrapper<MemberEntity>().eq("social_uid", socialUser.getUid()));
        //存在
        if (member != null) {
            //1.1）更新社交数据
            member.setSocialUid(socialUser.getUid());
            member.setAccessToken(socialUser.getAccess_token());
            member.setExpiresIn(socialUser.getExpires_in());
            this.updateById(member);
            return member;
        } else {
            //1.2）注册社交登录的账号
            MemberEntity memberEntity = new MemberEntity();
            //获取微博中的用户信息
            Map<String, String> query = new HashMap<>();
            query.put("access_token", socialUser.getAccess_token());
            query.put("uid", socialUser.getUid());

            try {
                HttpResponse response = HttpUtils.doGet("https://api.weibo.com", "/2/users/show.json", "get", new HashMap<>(), query);
                if (response.getStatusLine().getStatusCode() == 200) {
                    //将微博的信息获取转换为需要的字段
                    String json = EntityUtils.toString(response.getEntity());
                    JSONObject jsonObject = JSON.parseObject(json);

                    String name = (String) jsonObject.get("name");
                    String gender = (String) jsonObject.get("gender");

                    memberEntity.setNickname(name);
                    memberEntity.setGender(gender.equals("m") ? 1 : 0);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            memberEntity.setSocialUid(socialUser.getUid());
            memberEntity.setAccessToken(socialUser.getAccess_token());
            memberEntity.setExpiresIn(socialUser.getExpires_in());

            //设置默认字段
            memberEntity.setLevelId(memberLevelService.getDefaultLevel().getId());
            memberEntity.setCreateTime(new Date());

            this.save(memberEntity);

            return memberEntity;
        }
    }
}