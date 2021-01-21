package com.dadazhang.gulimall.authserver.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dadazhang.common.constant.AuthServerConstant;
import com.dadazhang.common.utils.HttpUtils;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.authserver.feign.MemberFeignService;
import com.dadazhang.common.vo.MemberVo;
import com.dadazhang.gulimall.authserver.vo.SocialUser;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/auth2.0")
@Controller
public class Auth2Controller {

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping("/weibo/success")
    public String weibo(@RequestParam("code") String code, HttpSession session) throws Exception {

        Map<String, String> tokenMap = new HashMap<>();

        tokenMap.put("client_id", "1730487551");
        tokenMap.put("client_secret", "cdfb68173fc973d7e0048e1c9d7106be");
        tokenMap.put("grant_type", "authorization_code");
        tokenMap.put("redirect_uri", "http://auth.gulimall.com/auth2.0/weibo/success");
        tokenMap.put("code", code);

        //1.）获取access_token
        HttpResponse response = HttpUtils.doPost("https://api.weibo.com", "/oauth2/access_token", "post", new HashMap<>(), new HashMap<>(), tokenMap);

        //2。）处理响应请求
        if (response.getStatusLine().getStatusCode() == 200) {
            //2.1）获取响应的json数据
            String json = EntityUtils.toString(response.getEntity());
            //2.2）将响应的json数据转换为对象
            SocialUser socialUser = JSON.parseObject(json, SocialUser.class);
            //2.3）调用远程member服务，进行登录
            R r = memberFeignService.socialLogin(socialUser);
            if (r.getCode() == 0) {
                MemberVo memberVo = r.getData(new TypeReference<MemberVo>() {
                });
                // TODO:1.）默认发令牌。session=1244422。 作用域：在当前域下，需要  解决子域的session共享问题
                // TODO:2.）使用JSON 序列化方式来实现对象数据的redis存储
                session.setAttribute(AuthServerConstant.SESSION_KEY, memberVo);
            }
        } else {
            //响应不成功重新进入登录页
            return "redirect:http://auth.gulimall.com/";
        }
        return "redirect:http://gulimall.com/";
    }
}
