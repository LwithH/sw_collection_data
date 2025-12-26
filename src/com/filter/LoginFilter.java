package com.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.model.User;

// 只拦截核心业务页面，不拦截登录/注册页
@WebFilter(urlPatterns = {"/index.jsp", "/SubmitDataServlet", "/ListDataServlet", "/list.jsp", "/result.jsp"})
public class LoginFilter implements Filter {
    @Override
    public void destroy() {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpSession session = req.getSession();

        // 1. 检查登录状态
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            // 2. 未登录：跳登录页，传递原目标页（避免登录后迷路）
            String currentUrl = req.getRequestURI();
            String contextPath = req.getContextPath();
            // 截取项目名后的路径（如/FormManagement/index.jsp → /index.jsp）
            if (currentUrl.startsWith(contextPath)) {
                currentUrl = currentUrl.substring(contextPath.length());
            }
            // 跳登录页（带redirect参数）
            resp.sendRedirect(contextPath + "/login.jsp?redirect=" + currentUrl);
            return;
        }

        // 3. 已登录：放行
        chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig fConfig) throws ServletException {}
}