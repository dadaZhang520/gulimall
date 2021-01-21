package com.dadazhang.gulimall.order.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.dadazhang.common.exception.BizCodeEnum;
import com.dadazhang.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
public class MySentinelConfig implements BlockExceptionHandler {

    @Override
    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws Exception {
        log.info("流控返回。。。。");
        R error = R.error(BizCodeEnum.CUSTOM_EXCEPTION.getCode(), BizCodeEnum.CUSTOM_EXCEPTION.getMessage());
        httpServletResponse.setCharacterEncoding("utf-8");
        httpServletResponse.setContentType("application/json");
        httpServletResponse.getWriter().println(JSON.toJSONString(error));
    }
}
