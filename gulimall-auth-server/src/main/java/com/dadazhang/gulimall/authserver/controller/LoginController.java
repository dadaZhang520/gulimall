package com.dadazhang.gulimall.authserver.controller;

import com.alibaba.fastjson.TypeReference;
import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.utils.R;
import com.dadazhang.common.constant.AuthServerConstant;
import com.dadazhang.gulimall.authserver.feign.MemberFeignService;
import com.dadazhang.gulimall.authserver.feign.SmsCodeService;
import com.dadazhang.common.vo.MemberVo;
import com.dadazhang.gulimall.authserver.vo.UserLoginVo;
import com.dadazhang.gulimall.authserver.vo.UserRegisterVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Slf4j
@Controller
public class LoginController {

    @Autowired
    SmsCodeService smsCodeService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    MemberFeignService memberFeignService;

    @GetMapping({"/", "/login.html"})
    public String loginPage(HttpSession session) {
        return session.getAttribute(AuthServerConstant.SESSION_KEY) != null ? "redirect:http://gulimall.com/" : "login";
    }

    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam("phone") String phone) {

        //todo: 1。）接口防刷
        if (StringUtils.isEmpty(phone)) {
            return R.error(BizCodeEnum.SMS_PHONE_EXCEPTION.getCode(), BizCodeEnum.SMS_PHONE_EXCEPTION.getMessage());
        }

        //2.）60秒内不可发送第二次验证码

        String key = AuthServerConstant.REDIS_SMS_CODE_KEY + phone;
        String redisCode = stringRedisTemplate.opsForValue().get(key);
        if (redisCode != null) {
            long time = Long.parseLong(redisCode.split("_")[1]);
            if (System.currentTimeMillis() - time < 60000) {
                return R.error(BizCodeEnum.SMS_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_CODE_EXCEPTION.getMessage());
            }
        }

        //3。）发送验证码，并保存到redis中
        int random = new Random().nextInt(999999);
        String code = Integer.toString(random);
        String val = code + "_" + System.currentTimeMillis();
        stringRedisTemplate.opsForValue()
                .set(key, val, 15, TimeUnit.MINUTES);

        return smsCodeService.sendSmsCode(phone, code);

    }

    /**
     * //TODO: 重定向携带数据，利用session原理。将数据放在session中。
     * 只要跳到下一个页面看去除这个数据以后，session里面的数据就会删掉。
     * <p>
     * //TODO 1.分布式下的session问题
     *
     * @param userRegisterVo
     * @param result
     * @param redirectAttributes
     * @return
     */
    @PostMapping("/regis")
    public String regis(@Valid UserRegisterVo userRegisterVo, BindingResult result, RedirectAttributes redirectAttributes) {
        Map<String, String> map = new HashMap<>();
        //1.）JRS330后端校验表单数据
        if (result.hasErrors()) {
            result.getFieldErrors().forEach(error -> {
                map.put(error.getField(), error.getDefaultMessage());
            });

            redirectAttributes.addFlashAttribute("errors", map);
            redirectAttributes.addFlashAttribute("user", userRegisterVo);

            return "redirect:http://auth.gulimall.com/register.html";
        }

        // 2.）检验验证码是否正确
        String redisCode = stringRedisTemplate.opsForValue().get(AuthServerConstant.REDIS_SMS_CODE_KEY + userRegisterVo.getPhone());
        if (!StringUtils.isEmpty(redisCode)) {
            if (redisCode.split("_")[0].equalsIgnoreCase(userRegisterVo.getCode())) {

                //3.)调用远程注册服务
                R r = memberFeignService.register(userRegisterVo);
                if (r.getCode() == 0) {
                    //2.1）删除验证码
                    Boolean delete = stringRedisTemplate.delete(AuthServerConstant.REDIS_SMS_CODE_KEY + userRegisterVo.getPhone());

                    log.info("注册成功");
                } else {
                    map.put("msg", r.getMsg());
                    redirectAttributes.addFlashAttribute("errors", map);
                    redirectAttributes.addFlashAttribute("user", userRegisterVo);

                    return "redirect:http://auth.gulimall.com/register.html";
                }
            } else {
                map.put("code", "验证码错误");
                redirectAttributes.addFlashAttribute("errors", map);
                redirectAttributes.addFlashAttribute("user", userRegisterVo);

                return "redirect:http://auth.gulimall.com/register.html";
            }
        } else {
            map.put("code", "验证码错误");
            redirectAttributes.addFlashAttribute("errors", map);
            redirectAttributes.addFlashAttribute("user", userRegisterVo);

            return "redirect:http://auth.gulimall.com/register.html";
        }
        return "redirect:http://auth.gulimall.com/login.html";
    }

    @PostMapping("/login")
    public String login(UserLoginVo userLoginVo, RedirectAttributes redirectAttributes, HttpSession session) {

        Map<String, String> errors = new HashMap<>();

        //2.）调用远程登录
        R r = memberFeignService.login(userLoginVo);

        if (r.getCode() == 0) {
            //获取登录后的用户信息
            MemberVo memberVo = r.getData(new TypeReference<MemberVo>() {
            });
            session.setAttribute(AuthServerConstant.SESSION_KEY, memberVo);
        } else {
            errors.put("username", userLoginVo.getUsername());
            errors.put("msg", r.getMsg());
            redirectAttributes.addFlashAttribute("errors", errors);

            return "redirect:http://auth.gulimall.com/login.html";
        }
        return "redirect:http://gulimall.com/";
    }
}
