package com.servlet;

import com.dao.AmazonDataDAO;
import com.dao.SellerNameDAO;
import com.google.gson.Gson;
import com.model.BatchAddRequest;
import com.model.User;
import com.util.OperationLogger; // ← 新增导入

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/BatchAddAmazonDataServlet")
public class BatchAddAmazonDataServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        if (user == null || !"yes".equalsIgnoreCase(user.getIsSeeAmazon())) {
            out.print(gson.toJson(new Result(false, "无权操作")));
            return;
        }

        try {
            String json = request.getReader().lines().reduce("", (a, b) -> a + b);
            BatchAddRequest req = gson.fromJson(json, BatchAddRequest.class);

            if (req.getPrefix() == null || req.getPrefix().isEmpty()) {
                out.print(gson.toJson(new Result(false, "账户前缀不能为空")));
                return;
            }
            if (req.getSku() == null || req.getSku().isEmpty()) {
                out.print(gson.toJson(new Result(false, "SKU不能为空")));
                return;
            }
            if (req.getSeller() == null || req.getSeller().isEmpty()) {
                out.print(gson.toJson(new Result(false, "Seller不能为空")));
                return;
            }

            SellerNameDAO sellerDao = new SellerNameDAO();
            if (!sellerDao.isValidSeller(req.getSeller())) {
                out.print(gson.toJson(new Result(false, "Seller 不存在或已停用")));
                return;
            }

            String userIdDing;
            if (sellerDao.isSellerDuplicated(req.getSeller())) {
                List<String> allowedDingIds = sellerDao.getAllUserIdDingsBySeller(req.getSeller());
                String currentUserDingId = user.getUserIdDing();
                if (currentUserDingId == null || !allowedDingIds.contains(currentUserDingId)) {
                    out.print(gson.toJson(new Result(false, "❌ 您无权为他人负责的重名 Seller [" + req.getSeller() + "] 批量新增数据")));
                    return;
                }
                userIdDing = currentUserDingId;
            } else {
                userIdDing = sellerDao.validateSellerAndGetUserIdDing(req.getSeller());
                if (userIdDing == null) {
                    out.print(gson.toJson(new Result(false, "Seller 未关联有效用户")));
                    return;
                }
            }

            AmazonDataDAO dataDAO = new AmazonDataDAO();
            int successCount = dataDAO.batchInsertAmazonData(req, user.getId(), userIdDing);

            if (successCount > 0) {
                // ========== 【新增】记录批量操作日志 ==========
                String operator = user.getUsername(); // 或 getName()
                Integer userId = user.getId();

                // 获取实际涉及的账户列表（用于日志详情）
                List<String> affectedUacsList = dataDAO.getUacsByPrefix(req.getPrefix());


                // 生成中文描述
                String description = String.format(
                    "用户 [%s] 批量新增了 %d 条亚马逊记录：账户前缀=%s, SKU=%s, Seller=%s",
                    operator,
                    successCount,
                    formatValue(req.getPrefix()),
                    formatValue(req.getSku()),
                    formatValue(req.getSeller())
                );

                // 构建结构化日志数据
                Map<String, Object> logDetails = new HashMap<>();
                logDetails.put("prefix", req.getPrefix());
                logDetails.put("sku", req.getSku());
                logDetails.put("seller", req.getSeller());
                logDetails.put("warehouseSku", req.getWarehouseSku());
                logDetails.put("asin", req.getAsin());
                logDetails.put("parentAsin", req.getParentAsin());
                logDetails.put("lifecl", req.getLifecl());
                logDetails.put("successCount", successCount);
                logDetails.put("affectedUacs", affectedUacsList); // 账户列表
                logDetails.put("userIdDing", userIdDing);
                logDetails.put("createUserId", userId);

                // 记录日志（自动包含 IP + User-Agent）
                OperationLogger.log(
                    request,
                    operator,
                    userId,
                    "ADD_BATCH",
                    null, // 批量操作无单一 target_id
                    description,
                    logDetails
                );

                out.print(gson.toJson(new BatchResult(true, "批量新增成功", successCount)));
            } else {
                out.print(gson.toJson(new Result(false, "批量新增失败")));
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print(gson.toJson(new Result(false, "服务器错误: " + e.getMessage())));
        }
    }

    // ===== 辅助方法：格式化显示值 =====
    private static String formatValue(String value) {
        return (value == null || value.trim().isEmpty()) ? "(空)" : value;
    }

    // ===== 内部类 =====
    static class Result {
        boolean success;
        String message;
        Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }

    static class BatchResult {
        boolean success;
        String message;
        int count;
        BatchResult(boolean success, String message, int count) {
            this.success = success;
            this.message = message;
            this.count = count;
        }
    }
}
