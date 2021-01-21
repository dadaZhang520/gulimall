package com.dadazhang.gulimall.member.exception;

import com.dadazhang.common.exception.BizCodeEnum;

public class UserLoginNameException extends RuntimeException {

    public UserLoginNameException() {
        super(BizCodeEnum.USER_LOGIN_NAME_NOT_FOUND_EXCEPTION.getMessage());
    }
}
