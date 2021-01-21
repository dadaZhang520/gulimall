package com.dadazhang.gulimall.seckill.web;

import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.seckill.service.SeckillService;
import com.dadazhang.gulimall.seckill.to.SeckillRedisTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SeckillController {

    @Autowired
    SeckillService seckillService;

    @ResponseBody
    @GetMapping("/current/seckill/sku")
    public R currentSeckillSku() {
        List<SeckillRedisTo> seckillRedisTos = seckillService.currentSeckillSku();
        return R.ok().setData(seckillRedisTos);
    }

    @ResponseBody
    @GetMapping("/get/seckill/bySkuId")
    public R getSeckillBySkuId(@RequestParam Long skuId) {
        SeckillRedisTo seckillRedisTo = seckillService.getSeckillBySkuId(skuId);
        return R.ok().setData(seckillRedisTo);
    }

    @GetMapping("/seckill")
    public String secKill(@RequestParam("sessionId") Long sessionId,
                          @RequestParam("skuId") Long skuId,
                          @RequestParam("code") String code,
                          @RequestParam("shopNum") Integer shopNum,
                          Model model) throws InterruptedException {

        String orderSn = seckillService.secKill(sessionId, skuId, code, shopNum);
        model.addAttribute("orderSn", orderSn);

        return "success";
    }
}
