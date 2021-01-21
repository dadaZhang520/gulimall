package com.dadazhang.gulimall.authserver.vo;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Data
public class UserLoginVo {

    @NotEmpty(message = "用户名必须提交")
    @Length(min = 4,max = 10,message = "长度需要在4和10之间")
    private String username;

    @NotEmpty(message = "密码必须填写")
    @Length(min = 6,max = 18,message = "密码长度需要在6和18之间")
    private String password;
}
