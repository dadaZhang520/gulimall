package com.dadazhang.gulimall.order.web;

import com.alipay.api.AlipayApiException;
import com.dadazhang.gulimall.order.exception.NoStockException;
import com.dadazhang.gulimall.order.service.OrderService;
import com.dadazhang.gulimall.order.util.AlipayTemplate;
import com.dadazhang.gulimall.order.vo.OrderConfirmVo;
import com.dadazhang.gulimall.order.vo.OrderSubmitVo;
import com.dadazhang.gulimall.order.vo.OrderSubmitResponseVo;
import com.dadazhang.gulimall.order.vo.PayVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.concurrent.ExecutionException;

@Controller
public class OrderWebController {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    @GetMapping("/{page}.html")
    public String confirm(@PathVariable("page") String page) {

        return page;
    }

    @GetMapping("/to/trade")
    public String toTrade(Model model) throws ExecutionException, InterruptedException {

        OrderConfirmVo orderConfirmVo = orderService.confirmOrder();

        model.addAttribute("orderConfirmData", orderConfirmVo);

        return "confirm";
    }

    @PostMapping("/submit/order")
    public String submitOrder(OrderSubmitVo orderSubmitVo, Model model, RedirectAttributes redirectAttributes) {
        //判断订单是否成功
        try {
            OrderSubmitResponseVo responseVo = orderService.submitOrder(orderSubmitVo);
            if (responseVo.getCode() == 0) {
                model.addAttribute("order", responseVo.getOrder());
                return "pay";
            }
        } catch (NoStockException e) {
            redirectAttributes.addFlashAttribute("msg", e.getMessage());
        }
        return "redirect:http://order.gulimall.com/to/trade";
    }

    @ResponseBody
    @GetMapping(value = "/pay/order/{orderSn}", produces = "text/html")
    public String payOrder(@PathVariable("orderSn") String orderSn) throws AlipayApiException {

        //构造支付对象
        PayVo payVo = orderService.payOrder(orderSn);
        //阿里支付页面
        String pay = alipayTemplate.pay(payVo);

        return pay;
    }
}
