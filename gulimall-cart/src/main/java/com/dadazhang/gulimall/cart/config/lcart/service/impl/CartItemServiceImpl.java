package com.dadazhang.gulimall.cart.config.lcart.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.dadazhang.common.constant.CartConstant;
import com.dadazhang.common.to.UserInfoTo;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.cart.config.lcart.feign.ProductFeignService;
import com.dadazhang.gulimall.cart.config.lcart.interceptor.CartInterceptor;
import com.dadazhang.gulimall.cart.config.lcart.service.CartItemService;
import com.dadazhang.gulimall.cart.config.lcart.vo.Cart;
import com.dadazhang.gulimall.cart.config.lcart.vo.CartItem;
import com.dadazhang.gulimall.cart.config.lcart.vo.SkuInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CartItemServiceImpl implements CartItemService {

    @Autowired
    ProductFeignService productFeignService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    ThreadPoolExecutor executor;

    /**
     * 添加购物车商品
     *
     * @param skuId
     * @param shopNum
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public void addToCart(Long skuId, Integer shopNum) throws ExecutionException, InterruptedException {

        //获取操作Hash的redis对象
        BoundHashOperations<String, Object, Object> ops = getCartOps();

        //1.）判断redis中是否存在这个商品
        String shop = (String) ops.get(skuId.toString());
        if (!StringUtils.isEmpty(shop)) {
            // 2）存在就修改当前商品在购物车的数量
            CartItem cartItem = JSON.parseObject(shop, CartItem.class);
            cartItem.setCount(cartItem.getCount() + shopNum);

            //2.1）将修改后的商品添加到购物车存储
            String jsonString = JSON.toJSONString(cartItem);
            ops.put(skuId.toString(), jsonString);

            cartItem.setCount(shopNum);
        } else {
            CartItem cartItem = new CartItem();

            //2.）调用远程服务查询sku的信息
            CompletableFuture<Void> skuInfoFuture = CompletableFuture.runAsync(() -> {
                R skuInfoR = productFeignService.info(skuId);
                if (skuInfoR.getCode() == 0) {

                    SkuInfoVo skuInfoVo = skuInfoR.getData(new TypeReference<SkuInfoVo>() {
                    });

                    cartItem.setSkuId(skuId);
                    cartItem.setCount(shopNum);
                    cartItem.setPrice(skuInfoVo.getPrice());
                    cartItem.setTitle(skuInfoVo.getSkuTitle());
                    cartItem.setImg(skuInfoVo.getSkuDefaultImg());
                }
            }, executor);

            //3.）远程调用查询当前skuid下的属性
            CompletableFuture<Void> skuSaleAttrFuture = CompletableFuture.runAsync(() -> {
                R skuSaleAttrR = productFeignService.getSkuSaleAttrBySkuId(skuId);

                if (skuSaleAttrR.getCode() == 0) {
                    List<String> vos = skuSaleAttrR.getData(new TypeReference<List<String>>() {
                    });

                    cartItem.setSaleAttr(vos);
                }
            }, executor);

            //3.1）等待异步任务完成
            CompletableFuture<Void> future = CompletableFuture.allOf(skuInfoFuture, skuSaleAttrFuture);
            future.get();

            //4.将商品添加到购物车存储
            String jsonString = JSON.toJSONString(cartItem);
            ops.put(skuId.toString(), jsonString);
        }
    }

    /**
     * 获取购物车信息
     *
     * @param text
     * @return
     * @throws ExecutionException
     * @throws InterruptedException
     */
    @Override
    public Cart getCart(String text) throws ExecutionException, InterruptedException {
        Cart cart = new Cart();
        //1.）判断是否登录了
        UserInfoTo userInfoTo = CartInterceptor.localData.get();
        //1.1）登录就合并购物车
        String tempCartKey = CartConstant.CART_PREFIX + userInfoTo.getUserKey();
        String userCartKey = CartConstant.CART_PREFIX + userInfoTo.getUserId();
        if (userInfoTo.getUserId() != null) {
            //1.2）判断购物车中的商品是否合并
            List<CartItem> cartItems = getCartItem(tempCartKey);
            //1.3）没有合并就添加到购物车
            if (cartItems.size() > 0) {
                for (CartItem item : cartItems) {
                    //就添加到购物车
                    addToCart(item.getSkuId(), item.getCount());
                }
                //清空购物车
                clearCart(tempCartKey);
            }
            //1.4）查询出合并后的购物车
            List<CartItem> cartItems1 = getCartItem(userCartKey);
            cart.setItems(cartItems1);
        } else { //2）没有登录就只查询临时用户购物车
            //临时用户购物车信息
            List<CartItem> cartItems1 = getCartItem(tempCartKey);
            cart.setItems(cartItems1);
        }
        //3.）如果text不为空，就要进行匹配查询
        if (!StringUtils.isEmpty(text) && cart.getItems().size() > 0) {
            List<CartItem> items = cart.getItems().stream().filter(item -> {
                if (item.getTitle().contains(text) ||
                        (text.trim().substring(0, text.indexOf(".")).matches("^[0-9]*$") &&
                                item.getPrice().compareTo(new BigDecimal(text).setScale(1, RoundingMode.HALF_UP)) == 0)) {
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
            cart.setItems(items);
        }

        return cart;
    }

    /**
     * 获取购物车的某件商品
     *
     * @param skuId
     * @return
     */
    @Override
    public CartItem getCartItemBySkuId(Long skuId) {
        CartItem cartItem = new CartItem();
        //获取购物车中的商品，通过skuId
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String json = (String) cartOps.get(skuId.toString());
        if (!StringUtils.isEmpty(json)) {
            cartItem = JSON.parseObject(json, CartItem.class);
        }
        return cartItem;
    }

    /**
     * 清空购物车
     */
    @Override
    public void clearCart(String cartKey) {
        stringRedisTemplate.delete(cartKey);
    }

    /**
     * 修改选中状态
     *
     * @param skuId
     * @param checked
     */
    @Override
    public void check(Long skuId, Integer checked) {

        //1.）获取当前商品
        CartItem cartItem = getCartItemBySkuId(skuId);
        cartItem.setCheck(checked == 1);

        //2.）更新商品是否选中
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String json = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), json);
    }

    /**
     * 修改商品数量
     *
     * @param skuId
     * @param num
     */
    @Override
    public void opsCount(Long skuId, Integer num) {
        //1.）获取当前商品
        CartItem cartItem = getCartItemBySkuId(skuId);
        cartItem.setCount(num);

        //2.）更新商品数量
        BoundHashOperations<String, Object, Object> cartOps = getCartOps();
        String json = JSON.toJSONString(cartItem);
        cartOps.put(skuId.toString(), json);
    }

    /**
     * 通过skuId删除商品
     *
     * @param skuId
     */
    @Override
    public void removeCartItem(Long skuId) {
        BoundHashOperations<String, Object, Object> ops = getCartOps();
        Long num = ops.delete(skuId.toString());
        System.out.println(num);
    }

    @Override
    public List<CartItem> getUserCartItem() {
        UserInfoTo userInfoTo = CartInterceptor.localData.get();

        if (userInfoTo.getUserId() != null) {
            List<CartItem> cartItem = getCartItem(CartConstant.CART_PREFIX + userInfoTo.getUserId());
            return cartItem.stream()
                    .filter(CartItem::isCheck)
                    .peek(item -> item.setPrice(productFeignService.getPriceById(item.getSkuId())))
                    .collect(Collectors.toList());
        }
        return null;
    }

    /**
     * 通过cartkey获取购物车的商品
     *
     * @param cartKey
     * @return
     */
    private List<CartItem> getCartItem(String cartKey) {

        List<CartItem> cartItems = new ArrayList<>();

        BoundHashOperations<String, Object, Object> ops = stringRedisTemplate.boundHashOps(cartKey);

        Set<Object> keys = ops.keys();
        if (keys != null && keys.size() > 0) {
            List<Object> val = ops.multiGet(keys);
            if (val != null && val.size() > 0) {
                cartItems = val.stream().map(item -> {
                    String json = item.toString();
                    return JSON.parseObject(json, CartItem.class);
                }).collect(Collectors.toList());
            }
        }
        return cartItems;
    }

    private BoundHashOperations<String, Object, Object> getCartOps() {
        //1.）加入购物车前先判断是否登录
        UserInfoTo userInfoTo = CartInterceptor.localData.get();

        String cartKey = userInfoTo.getUserId() == null ?
                CartConstant.CART_PREFIX + userInfoTo.getUserKey() :
                CartConstant.CART_PREFIX + userInfoTo.getUserId();

        //2.）加入购物车前需要先判断当前用加入的商品是否已存在购物车中
        return stringRedisTemplate.boundHashOps(cartKey);
    }

}
