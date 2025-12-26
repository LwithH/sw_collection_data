package com.servlet;

import com.dao.AmazonDataDAO;
import com.google.gson.Gson;
//判断多个新增 uacs和sku唯一索引
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/CheckBatchSkuDuplicateServlet")
public class CheckBatchSkuDuplicateServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        try {
            // 读取请求数据
            String json = request.getReader().lines().reduce("", (a, b) -> a + b);
            BatchSkuCheckRequest req = gson.fromJson(json, BatchSkuCheckRequest.class);
            
            if (req.sku == null || req.sku.trim().isEmpty()) {
                out.print(gson.toJson(new BatchSkuCheckResult(false, "SKU不能为空", 0, null)));
                return;
            }
            
            if (req.prefix == null || req.prefix.trim().isEmpty()) {
                out.print(gson.toJson(new BatchSkuCheckResult(false, "账户前缀不能为空", 0, null)));
                return;
            }
            
            // 检查批量SKU重复性
            AmazonDataDAO dataDAO = new AmazonDataDAO();
            
            // 获取前缀对应的所有账户
            List<String> uacsList = dataDAO.getUacsByPrefix(req.prefix.trim());
            if (uacsList.isEmpty()) {
                out.print(gson.toJson(new BatchSkuCheckResult(true, "没有找到对应的账户", 0, null)));
                return;
            }
            
            // 检查每个账户中SKU是否重复：同一个账户下相同的SKU
            int existsCount = 0;
            java.util.ArrayList<String> duplicateUacs = new java.util.ArrayList<>();
            
            for (String uacs : uacsList) {
                if (dataDAO.isSkuDuplicateInUacs(req.sku.trim(), uacs)) {
                    existsCount++;
                    duplicateUacs.add(uacs);
                }
            }
            
            if (existsCount > 0) {
                out.print(gson.toJson(new BatchSkuCheckResult(
                    true, 
                    "该SKU在 " + existsCount + " 个账户中已存在", 
                    existsCount, 
                    duplicateUacs
                )));
            } else {
                out.print(gson.toJson(new BatchSkuCheckResult(
                    true, 
                    "SKU在所有账户中均可用", 
                    0, 
                    null
                )));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            out.print(gson.toJson(new BatchSkuCheckResult(false, "检查服务异常: " + e.getMessage(), 0, null)));
        }
    }
    
    // 内部类：接收请求数据
    static class BatchSkuCheckRequest {
        String sku;
        String prefix;
    }
    
    // 内部类：返回检查结果
    static class BatchSkuCheckResult {
        boolean success;
        String message;
        int existsCount; // 重复的账户数量
        java.util.List<String> duplicateUacs; // 重复的账户列表
        
        BatchSkuCheckResult(boolean success, String message, int existsCount, java.util.List<String> duplicateUacs) {
            this.success = success;
            this.message = message;
            this.existsCount = existsCount;
            this.duplicateUacs = duplicateUacs;
        }
    }
}