package com.dadazhang.gulimall.member.exception;

import com.dadazhang.common.exception.BizCodeEnum;

public class UserPhoneExistException extends RuntimeException{

    public UserPhoneExistException() {

        super(BizCodeEnum.USER_PHONE_EXIST_EXCEPTION.getMessage());
    }
}
