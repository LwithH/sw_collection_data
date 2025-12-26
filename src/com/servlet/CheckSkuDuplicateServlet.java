package com.servlet;
//判断单个新增 uacs和sku唯一索引
import com.dao.AmazonDataDAO;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/CheckSkuDuplicateServlet")
public class CheckSkuDuplicateServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try {
            // 读取请求数据
            String json = request.getReader().lines().reduce("", (a, b) -> a + b);
            SkuCheckRequest req = gson.fromJson(json, SkuCheckRequest.class);
            
            if (req.sku == null || req.sku.trim().isEmpty()) {
                out.print(gson.toJson(new SkuCheckResult(false, "SKU不能为空", false)));
                return;
            }
            
            if (req.uacs == null || req.uacs.trim().isEmpty()) {
                out.print(gson.toJson(new SkuCheckResult(false, "账户不能为空", false)));
                return;
            }
            
            // 检查SKU重复性
            AmazonDataDAO dataDAO = new AmazonDataDAO();
            boolean exists = dataDAO.isSkuDuplicateInUacs(req.sku.trim(), req.uacs.trim());
            
            if (exists) {
                out.print(gson.toJson(new SkuCheckResult(true, "SKU在此账户中已存在", true)));
            } else {
                out.print(gson.toJson(new SkuCheckResult(true, "SKU可用", false)));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            out.print(gson.toJson(new SkuCheckResult(false, "检查服务异常: " + e.getMessage(), false)));
        }
    }
    
    // 内部类：接收请求数据
    static class SkuCheckRequest {
        String sku;
        String uacs;
    }
    
    // 内部类：返回检查结果
    static class SkuCheckResult {
        boolean success;
        String message;
        boolean exists; // 是否存在重复
        
        SkuCheckResult(boolean success, String message, boolean exists) {
            this.success = success;
            this.message = message;
            this.exists = exists;
        }
    }
}