package com.dadazhang.gulimall.cart.config.lcart.controller;

import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.cart.config.lcart.service.CartItemService;
import com.dadazhang.gulimall.cart.config.lcart.vo.Cart;
import com.dadazhang.gulimall.cart.config.lcart.vo.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.concurrent.ExecutionException;

@Controller
public class CartController {

    @Autowired
    CartItemService cartItemService;

    /**
     * @return
     */
    @GetMapping("/cart.html")
    public String cartList(Model model,
                           @RequestParam(value = "text", required = false) String text)
            throws ExecutionException, InterruptedException {

        //1.）查询购物车商品
        Cart cart = cartItemService.getCart(text);

        model.addAttribute("cart", cart);
        model.addAttribute("text", text);

        return "cartList";
    }

    @GetMapping("/add/cart")
    public String addToCart(@RequestParam("skuId") Long skuId,
                            @RequestParam("shopNum") Integer shopNum,
                            RedirectAttributes redirectAttributes) throws ExecutionException, InterruptedException {

        //1.）将商品添加到购物车
        cartItemService.addToCart(skuId, shopNum);

        redirectAttributes.addAttribute("skuId", skuId);

        return "redirect:http://cart.gulimall.com/success.html";
    }

    @GetMapping("/success.html")
    public String success(@RequestParam("skuId") Long skuId,
                          Model model) {

        CartItem cartItem = cartItemService.getCartItemBySkuId(skuId);

        model.addAttribute("item", cartItem);

        return "success";
    }

    @GetMapping("/check")
    public String check(@RequestParam("skuId") Long skuId,
                        @RequestParam("checked") Integer checked) {

        cartItemService.check(skuId, checked);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/opsCount")
    public String opsCount(@RequestParam("skuId") Long skuId,
                           @RequestParam("num") Integer num) {

        cartItemService.opsCount(skuId, num);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @GetMapping("/remove/cart/item")
    public String removeCartItem(@RequestParam("skuId") Long skuId) {

        cartItemService.removeCartItem(skuId);

        return "redirect:http://cart.gulimall.com/cart.html";
    }

    @ResponseBody
    @GetMapping("/user/cart/item")
    public R getUserCartItem() {
        List<CartItem> cartItems = cartItemService.getUserCartItem();
        return R.ok().setData(cartItems);
    }
}
