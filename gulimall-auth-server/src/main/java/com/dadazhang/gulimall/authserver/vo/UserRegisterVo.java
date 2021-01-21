package com.dadazhang.gulimall.authserver.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;

@Data
public class UserRegisterVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 4,max = 10,message = "长度需要在4和10之间")
    private String username;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 18,message = "密码长度需要在6和18之间")
    private String password;

    @Pattern(regexp = "^[1]([3-9])[0-9]{9}$",message = "手机号码不正确")
    private String phone;

    @NotEmpty(message = "验证码必须提交")
    private String code;

}
