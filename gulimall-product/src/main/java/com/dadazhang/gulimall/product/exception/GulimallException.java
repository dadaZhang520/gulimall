package com.dadazhang.gulimall.product.exception;

import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(basePackages = "com.dadazhang.gulimall.product.controller")
public class GulimallException {

    @ExceptionHandler
    public R handlerValidException(MethodArgumentNotValidException e) {
        log.info("参数校验异常信息{} 异常类型{}", e.getMessage(), e.getClass());
        Map<String, String> errorMap = new HashMap<>();
        BindingResult result = e.getBindingResult();
        result.getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });
        return R.error(BizCodeEnum.VALUE_EXCEPTION.getCode(), BizCodeEnum.VALUE_EXCEPTION.getMessage()).put("data", errorMap);
    }

    @ExceptionHandler
    public R handlerException(Throwable throwable) {
        log.info("未知异常信息{}",throwable.getMessage());
        return R.error(BizCodeEnum.UNKNOWN_EXCEPTION.getCode(), BizCodeEnum.UNKNOWN_EXCEPTION.getMessage());
    }
}
