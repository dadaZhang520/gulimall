package com.dadazhang.gulimall.ware.exception;

public class NoStockException extends RuntimeException {
    public NoStockException(Long skuId) {
        super("skuId:" + skuId + "没有库存");
    }
}
