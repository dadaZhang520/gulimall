package com.dadazhang.gulimall.member.exception;

import com.dadazhang.common.exception.BizCodeEnum;

public class UserNameExistException  extends RuntimeException{

    public UserNameExistException() {

       super(BizCodeEnum.USER_NAME_EXIST_EXCEPTION.getMessage());
    }
}
