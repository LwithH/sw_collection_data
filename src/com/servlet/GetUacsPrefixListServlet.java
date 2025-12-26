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
//亚马逊批量新增功能列表
@WebServlet("/GetUacsPrefixListServlet")
public class GetUacsPrefixListServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        
        try {
            // 从数据库获取去重后的账户前缀列表
            List<String> prefixList = new AmazonDataDAO().getDistinctUacsPrefixes();
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("prefixList", prefixList);
            
            response.getWriter().write(new Gson().toJson(result));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "获取账户前缀列表失败");
            response.getWriter().write(new Gson().toJson(result));
        }
    }
}