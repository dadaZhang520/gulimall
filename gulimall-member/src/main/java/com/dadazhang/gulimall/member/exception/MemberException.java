package com.dadazhang.gulimall.member.exception;

import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.utils.R;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice("com.dadazhang.gulimall.member.controller")
public class MemberException {

    @ExceptionHandler
    public R handlerException(RuntimeException runtimeException) {

        return R.error(BizCodeEnum.CUSTOM_EXCEPTION.getCode(), runtimeException.getMessage());
    }
}
