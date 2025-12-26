package com.servlet;

import java.io.IOException;
import java.net.URLEncoder; // 导入URL编码工具类

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/LogoutServlet")
public class LogoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // 1. 销毁Session，清除登录状态
        HttpSession session = request.getSession();
        session.invalidate();

        // 2. 对中文消息进行URL编码（关键：确保浏览器能正确传递中文）
        String logoutMsg = "success！";
        String encodedMsg = URLEncoder.encode(logoutMsg, "UTF-8");

        // 3. 跳转到登录页，携带编码后的消息
        String contextPath = request.getContextPath();
        response.sendRedirect(contextPath + "/login.jsp?msg=" + encodedMsg);
    }
}