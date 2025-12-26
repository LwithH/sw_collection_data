package com.servlet;
import com.dao.*;
import com.google.gson.Gson;
import com.model.AmazonData;
import com.model.User;
import com.servlet.AddAmazonDataServlet.Result;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//亚马逊获取账户列表
@WebServlet("/GetUacsByPrefixServlet")
public class GetUacsByPrefixServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        String prefix = request.getParameter("prefix");
        if (prefix == null || prefix.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "前缀参数不能为空");
            response.getWriter().write(new Gson().toJson(result));
            return;
        }
        
        try {
            // 根据前缀获取对应的所有账户
            List<String> uacsList = new AmazonDataDAO().getUacsByPrefix(prefix);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("uacsList", uacsList);
            
            response.getWriter().write(new Gson().toJson(result));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取账户列表失败");
            response.getWriter().write(new Gson().toJson(result));
        }
    }
}