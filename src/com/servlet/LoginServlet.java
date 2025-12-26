package com.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dao.UserDAO;
import com.model.User;
//登录
@WebServlet("/LoginServlet")
public class LoginServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. 获取参数（登录名、密码、跳转目标页）
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        String redirect = request.getParameter("redirect");

        // 2. 处理redirect：若为null，默认跳首页（关键：避免null路径）
        if (redirect == null || redirect.trim().isEmpty()) {
            redirect = "index.jsp";
        }

        // 3. 前端验证（判空）
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            request.setAttribute("msg", "登录名和密码不能为空！");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }

        // 4. 登录验证
        UserDAO userDAO = new UserDAO();
        User loginUser = userDAO.login(username, password);

        // 5. 登录成功：跳转（加项目路径，避免404）
        if (loginUser != null) {
            HttpSession session = request.getSession();
            session.setAttribute("loginUser", loginUser); // 存入登录状态

            // 关键：拼接项目路径（如/FormManagement/index.jsp）
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/" + redirect);
        } else {
            // 登录失败：返回登录页
            request.setAttribute("msg", "登录名或密码错误！");
            request.getRequestDispatcher("login.jsp").forward(request, response);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect("login.jsp");
    }
} 