package com.dadazhang.gulimall.authserver.feign;

import com.dadazhang.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("gulimall-third-party")
public interface SmsCodeService {

    @GetMapping("third/party/sms/policy")
    R sendSmsCode(@RequestParam("phone") String phone, @RequestParam("code") String code);
}
