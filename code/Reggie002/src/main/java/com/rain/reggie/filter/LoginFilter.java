package com.rain.reggie.filter;

import com.alibaba.fastjson.JSON;
import com.rain.reggie.common.BaseContext;
import com.rain.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@WebFilter(filterName = "loginFilter", urlPatterns = "/*")
public class LoginFilter implements Filter {
    /**
     * 1. 获取请求路径 URI(URL与URI的关系?)
     * 2. 查看 URI 是否可以直接放行, 例如登入登出, 静态资源页面.
     * 3. 否则查看 session 中是否包含 user id 信息.
     * 4. 不通过则需要返回错误信息, 否则放行.
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        String uri = request.getRequestURI();
        log.info("拦截到请求{}", uri);
        // 以下 uri 直接放行, 注意此处如若访问 backend 下的 index.html, 该页面的请求确实会被放行
        // 但与此同时访问该页面后还会请求员工分页查询数据, 而后者将会被拦截并进行校验.
        String[] ok_list = {
                "/employee/login",
                "/employee/logout",
                "/user/login",
                "/backend/**",
                "/front/**"
        };
        if (check(ok_list, uri)){
            log.info("本次请求{}不需要处理", uri);
            filterChain.doFilter(request, response);
            return;
        }
        if (null != request.getSession().getAttribute("employee")){
            log.info("管理员已登录");
            Long user_id = (Long) request.getSession().getAttribute("employee");
            BaseContext.setId(user_id);
            filterChain.doFilter(request, response);
            return;
        }
        if (null != request.getSession().getAttribute("user")){
            log.info("用户已登录");
            Long user_id = (Long) request.getSession().getAttribute("user");
            BaseContext.setId(user_id);
            filterChain.doFilter(request, response);
            return;
        }
        log.info("未登录");
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
    }

    private static boolean check(String[] ok_list, String uri){
        AntPathMatcher matcher = new AntPathMatcher();
        for (String u: ok_list){
            if (matcher.match(u, uri))
                return true;
        }
        return false;
    }
}
