package com.dadazhang.common.exception;

public enum BizCodeEnum {

    CUSTOM_EXCEPTION(99999,"自定义异常"),
    UNKNOWN_EXCEPTION(10000, "系统未知异常"),
    VALUE_EXCEPTION(10001, "参数校验异常"),
    SMS_CODE_EXCEPTION(10002, "验证码发送频率过高"),
    SMS_PHONE_EXCEPTION(10003, "发送手机号码为空"),
    PRODUCT_UP_EXCEPTION(11000, "商品上架异常"),
    USER_NAME_EXIST_EXCEPTION(15001,"用户名称已存在"),
    USER_PHONE_EXIST_EXCEPTION(15002,"用户手机号码已存在"),
    USER_LOGIN_NAME_NOT_FOUND_EXCEPTION(15003,"用户不存在"),
    USER_LOGIN_PASSWORD_NOT_MATCH_EXCEPTION(15004,"密码输入不正确"),
    NO_STOCK_EXCEPTION(20000,"商品库存不足"),
    SECKILL_FAILED(21000,"商品秒杀失败");

    private int code;

    private String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
