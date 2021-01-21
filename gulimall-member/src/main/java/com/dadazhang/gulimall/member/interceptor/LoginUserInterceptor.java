package com.dadazhang.gulimall.member.interceptor;

import com.dadazhang.common.constant.AuthServerConstant;
import com.dadazhang.common.vo.MemberVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<MemberVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String uri = request.getRequestURI();
        boolean match = new AntPathMatcher().match("/member/**", uri);
        if (match) {
            return true;
        }

        HttpSession session = request.getSession();
        MemberVo memberVo = (MemberVo) session.getAttribute(AuthServerConstant.SESSION_KEY);

        if (memberVo == null) {
            response.sendRedirect("http://auth.gulimall.com");
        }
        loginUser.set(memberVo);
        return true;
    }
}
