package com.dadazhang.gulimall.elasticsearch.web;

import com.dadazhang.gulimall.elasticsearch.service.MallSearchService;
import com.dadazhang.gulimall.elasticsearch.vo.SearchParamVo;
import com.dadazhang.gulimall.elasticsearch.vo.SearchResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {

    @Autowired
    MallSearchService mallSearchService;

    @GetMapping("/list.html")
    public String index(SearchParamVo param, Model model, HttpServletRequest request) {

        //获取路径参数
        param.setQueryString(request.getQueryString());

        SearchResultVo result = mallSearchService.search(param);

        model.addAttribute("result", result);
        return "list";
    }
}
