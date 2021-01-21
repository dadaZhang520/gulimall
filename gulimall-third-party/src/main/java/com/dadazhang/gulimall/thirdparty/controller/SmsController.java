package com.dadazhang.gulimall.thirdparty.controller;

import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.thirdparty.component.SmsCodeComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("third/party")
public class SmsController {

    @Autowired
    SmsCodeComponent smsCodeComponent;

    @GetMapping("/sms/policy")
    public R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {
        smsCodeComponent.sendSmsCode(phone, code);
        return R.ok();
    }
}
