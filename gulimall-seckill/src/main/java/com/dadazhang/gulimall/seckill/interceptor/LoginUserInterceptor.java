package com.dadazhang.gulimall.seckill.interceptor;

import com.dadazhang.common.constant.AuthServerConstant;
import com.dadazhang.common.vo.MemberVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        //放行指定请求
        String requestURI = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/seckill", requestURI);
        if (match) {
            HttpSession session = request.getSession();
            MemberVo member = (MemberVo) session.getAttribute(AuthServerConstant.SESSION_KEY);

            //如果没有登录就去登录,否则就放行
            if (member == null) {
                response.sendRedirect("http://auth.gulimall.com");
                return false;
            }
            loginUser.set(member);
        }
        return true;
    }
}
