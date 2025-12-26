package com.servlet;

import com.dao.AccountDAO;
import com.google.gson.Gson;
import com.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
//亚马逊数据表获取uacs
@WebServlet("/GetUacsListServlet")
public class GetUacsListServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 检查用户登录状态和权限
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        if (user == null || !"yes".equalsIgnoreCase(user.getIsSeeAmazon())) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();
        
        try {
            AccountDAO accountDAO = new AccountDAO();
            List<String> uacsList = accountDAO.getAllUacs();
            
            // 创建响应对象
            ResponseData responseData = new ResponseData();
            responseData.success = true;
            responseData.uacsList = uacsList;
            
            out.print(gson.toJson(responseData));
            System.out.println("成功返回 " + uacsList.size() + " 个UACS值");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("获取UACS列表时出错: " + e.getMessage());
            ResponseData responseData = new ResponseData();
            responseData.success = false;
            responseData.message = "服务器内部错误: " + e.getMessage();
            out.print(gson.toJson(responseData));
        }
    }
    
    // 响应数据结构
    private static class ResponseData {
        boolean success;
        List<String> uacsList;
        String message;
    }
}
