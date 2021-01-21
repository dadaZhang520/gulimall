package com.dadazhang.gulimall.member.web;

import com.dadazhang.common.utils.R;
import com.dadazhang.gulimall.member.feign.OrderFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;

@Controller
public class MemberWebController {

    @Autowired
    OrderFeignService orderFeignService;

    @GetMapping("/orderList.html")
    public String orderList(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                            Model model) {
        //构建分页查询的参数
        HashMap<String, Object> pageMap = new HashMap<>();
        pageMap.put("page", pageNum.toString());

        R r = orderFeignService.listWithItem(pageMap);
        if (r.getCode() == 0) {
            model.addAttribute("data", r.get("page"));
        }
        return "orderList";
    }
}
