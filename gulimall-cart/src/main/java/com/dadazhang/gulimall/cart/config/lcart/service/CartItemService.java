package com.dadazhang.gulimall.cart.config.lcart.service;

import com.dadazhang.gulimall.cart.config.lcart.vo.Cart;
import com.dadazhang.gulimall.cart.config.lcart.vo.CartItem;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface CartItemService {
    Cart getCart(String text) throws ExecutionException, InterruptedException;

    void addToCart(Long skuId, Integer shopNum) throws ExecutionException, InterruptedException;

    CartItem getCartItemBySkuId(Long skuId);

    void clearCart(String cartKey);

    void check(Long skuId, Integer checked);

    void opsCount(Long skuId, Integer num);

    void removeCartItem(Long skuId);

    List<CartItem> getUserCartItem();

}
