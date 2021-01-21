package com.dadazhang.gulimall.cart.config.lcart.interceptor;

import com.dadazhang.common.constant.AuthServerConstant;
import com.dadazhang.common.constant.CartConstant;
import com.dadazhang.common.to.UserInfoTo;
import com.dadazhang.common.vo.MemberVo;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

@Component
public class CartInterceptor implements HandlerInterceptor {

    public static final ThreadLocal<UserInfoTo> localData = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();

        //1.）判断用户是否登录
        HttpSession session = request.getSession();
        MemberVo memberVo = (MemberVo) session.getAttribute(AuthServerConstant.SESSION_KEY);
        if (memberVo != null) {
            userInfoTo.setUserId(memberVo.getId().toString());
        }

        //2.）判断当前购物车是否存在临时用户key
        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                String name = cookie.getName();
                if (name.equalsIgnoreCase(CartConstant.CART_TEMP_USER_KEY)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    userInfoTo.setTempUser(true);
                }
            }
        }
        //2.1）如果不存在临时key就分配一个
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString().replace("_", "");
            userInfoTo.setUserKey(uuid);
        }

        //3.）将用户的标识信息放在ThreadLocal进行数据共享
        localData.set(userInfoTo);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        //4.）分配的临时userKey需要在业务处理完成后响应回页面
        UserInfoTo userInfoTo = CartInterceptor.localData.get();
        if (!userInfoTo.isTempUser()) {
            Cookie userKey = new Cookie(CartConstant.CART_TEMP_USER_KEY, userInfoTo.getUserKey());
            userKey.setMaxAge(CartConstant.COOKIE_USER_KEY_EXPIRE_TIME);
            userKey.setDomain("gulimall.com");
            response.addCookie(userKey);
        }
    }
}
