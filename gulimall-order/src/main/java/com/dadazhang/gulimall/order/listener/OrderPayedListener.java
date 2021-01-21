package com.dadazhang.gulimall.order.listener;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.dadazhang.gulimall.order.service.OrderService;
import com.dadazhang.gulimall.order.util.AlipayTemplate;
import com.dadazhang.gulimall.order.vo.PayAsyncVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@RestController
public class OrderPayedListener {

    @Autowired
    OrderService orderService;

    @Autowired
    AlipayTemplate alipayTemplate;

    /**
     * 支付宝支付成功后的异步通知
     */
    @PostMapping("/order/pay/notify")
    public String aliPayedNotify(PayAsyncVo payAsyncVo,
                                 HttpServletRequest request) throws AlipayApiException {

        //1）、对支付宝返回的数据进行验签
        Map<String, String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用
//            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }

        boolean signVerified = AlipaySignature.rsaCheckV1(params, alipayTemplate.getAlipay_public_key(),
                alipayTemplate.getCharset(),
                alipayTemplate.getSign_type()); //调用SDK验证签名

        //验签成功
        if (signVerified) {
            //保存订单的交易流水
            System.out.println("验签成功。。。");
            orderService.payedNotify(payAsyncVo);
            return "success";
        }

        //只有返回success给支付宝服务器，支付宝才会停止重试继续发送支付成功的消息
        return "error";
    }
}
