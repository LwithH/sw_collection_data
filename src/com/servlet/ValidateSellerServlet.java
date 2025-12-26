package com.servlet;

import com.dao.SellerNameDAO;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/ValidateSellerServlet")
public class ValidateSellerServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try {
            // 读取请求数据
            String json = request.getReader().lines().reduce("", (a, b) -> a + b);
            SellerRequest req = gson.fromJson(json, SellerRequest.class);
            
            if (req.seller == null || req.seller.trim().isEmpty()) {
                out.print(gson.toJson(new ValidationResult(false, "Seller不能为空")));
                return;
            }
            
            SellerNameDAO sellerDao = new SellerNameDAO();
            
            // 验证Seller是否存在且有效
            if (!sellerDao.isValidSeller(req.seller)) {
                out.print(gson.toJson(new ValidationResult(false, "Seller不存在或已停用")));
                return;
            }
            
            // 验证Seller是否关联有效用户
            String userIdDing = sellerDao.validateSellerAndGetUserIdDing(req.seller);
            if (userIdDing == null) {
                out.print(gson.toJson(new ValidationResult(false, "Seller未关联有效用户")));
                return;
            }
            
            out.print(gson.toJson(new ValidationResult(true, "Seller有效")));
            
        } catch (Exception e) {
            e.printStackTrace();
            out.print(gson.toJson(new ValidationResult(false, "验证服务异常: " + e.getMessage())));
        }
    }
    
    // 内部类：接收请求数据
    static class SellerRequest {
        String seller;
    }
    
    // 内部类：返回验证结果
    static class ValidationResult {
        boolean success;
        String message;
        
        ValidationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}