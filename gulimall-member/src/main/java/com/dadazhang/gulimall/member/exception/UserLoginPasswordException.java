package com.dadazhang.gulimall.member.exception;

import com.dadazhang.common.exception.BizCodeEnum;

public class UserLoginPasswordException extends RuntimeException {

    public UserLoginPasswordException() {
        super(BizCodeEnum.USER_LOGIN_PASSWORD_NOT_MATCH_EXCEPTION.getMessage());
    }
}
