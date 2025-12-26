package com.servlet;

import com.dao.AmazonDataDAO;
import com.dao.SellerNameDAO;
import com.google.gson.Gson;
import com.model.AmazonData;
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
import java.util.*;
import java.util.stream.Collectors;

@WebServlet("/BatchUpdateAmazonDataServlet")
public class BatchUpdateAmazonDataServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        System.out.println("=== BatchUpdateAmazonDataServlet 开始执行 ===");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        if (user == null || !"yes".equalsIgnoreCase(user.getIsSeeAmazon())) {
            out.print(gson.toJson(new Result(false, "无权操作")));
            return;
        }

        try {
            String json = request.getReader().lines().reduce("", (a, b) -> a + b);
            System.out.println("接收到的批量JSON数据: " + json);

            BatchUpdateRequest req = gson.fromJson(json, BatchUpdateRequest.class);

            if (req.ids == null || req.ids.isEmpty()) {
                out.print(gson.toJson(new Result(false, "未选择任何记录")));
                return;
            }

            boolean shouldUpdateSeller = (req.seller != null);
            boolean shouldUpdateLifecl = (req.lifecl != null);

            SellerNameDAO sellerDao = new SellerNameDAO();
            String newUserIdDing = null;

            if (shouldUpdateSeller && !req.seller.trim().isEmpty()) {
                if (!sellerDao.isValidSeller(req.seller)) {
                    out.print(gson.toJson(new Result(false, "Seller 不存在或已停用")));
                    return;
                }

                if (sellerDao.isSellerDuplicated(req.seller)) {
                    List<String> allowedDingIds = sellerDao.getAllUserIdDingsBySeller(req.seller);
                    String currentUserDingId = user.getUserIdDing();
                    if (currentUserDingId == null || !allowedDingIds.contains(currentUserDingId)) {
                        out.print(gson.toJson(new Result(false, "❌ 您无权修改他人负责的重名 Seller [" + req.seller + "]")));
                        return;
                    }
                } else {
                    newUserIdDing = sellerDao.validateSellerAndGetUserIdDing(req.seller);
                    if (newUserIdDing == null) {
                        out.print(gson.toJson(new Result(false, "Seller 未关联有效用户")));
                        return;
                    }
                }
            }

            AmazonDataDAO dataDao = new AmazonDataDAO();
            List<AmazonData> originalList = dataDao.getOriginalDataBatch(req.ids);
            Map<Integer, AmazonData> originalMap = originalList.stream()
                .collect(Collectors.toMap(AmazonData::getId, a -> a));

            List<AmazonDataDAO.BatchUpdateItem> updateItems = new ArrayList<>();

            for (int id : req.ids) {
                AmazonData original = originalMap.get(id);
                if (original == null) continue;

                String finalSeller = original.getSeller();
                String finalLifecl = original.getLifecl();
                String finalUserIdDing = original.getUser_id_ding();
                String isc1Value = original.getIsc1();
                String finalSalesDepart = original.getSalesDepart();

                if (shouldUpdateSeller) {
                    String origSeller = original.getSeller();
                    if ((origSeller != null && !origSeller.trim().isEmpty() && !"-".equals(origSeller)) 
                        && (req.seller == null || req.seller.trim().isEmpty())) {
                        continue;
                    }

                    finalSeller = req.seller;

                    if (sellerDao.isSellerDuplicated(req.seller)) {
                        finalUserIdDing = user.getUserIdDing();
                    } else {
                        finalUserIdDing = newUserIdDing;
                    }

                    finalSalesDepart = sellerDao.getSalesDepartByUserIdDing(finalUserIdDing);
                    if (finalSalesDepart == null) {
                        finalSalesDepart = "0";
                    }

                    if (origSeller == null || origSeller.trim().isEmpty() || "-".equals(origSeller)) {
                        if (req.seller != null && !req.seller.trim().isEmpty() && !"-".equals(req.seller)) {
                            isc1Value = "首位";
                        }
                    } else if (req.seller != null && !req.seller.equals(origSeller)) {
                        isc1Value = "接手";
                    }
                }

                if (shouldUpdateLifecl) {
                    finalLifecl = req.lifecl;
                }

                updateItems.add(new AmazonDataDAO.BatchUpdateItem(
                    id,
                    finalSeller,
                    finalLifecl,
                    isc1Value,
                    finalUserIdDing,
                    finalSalesDepart
                ));
            }

            if (updateItems.isEmpty()) {
                out.print(gson.toJson(new Result(false, "无有效记录可更新")));
                return;
            }

            boolean success = dataDao.updateBatchSelective(updateItems, user.getId());

            if (success) {
                // ========== 【新增】记录批量修改日志 ==========
                String operator = user.getUsername(); // 或 getName()
                Integer userId = user.getId();
                int updatedCount = updateItems.size();

                // 构建变更摘要
                StringBuilder changeSummary = new StringBuilder();
                if (shouldUpdateSeller) {
                    changeSummary.append("Seller=").append(formatValue(req.seller));
                }
                if (shouldUpdateLifecl) {
                    if (changeSummary.length() > 0) changeSummary.append("；");
                    changeSummary.append("Lifecl=").append(formatValue(req.lifecl));
                }

                // 生成中文描述
                String description;
                if (updatedCount <= 5) {
                    // 少量：列出具体ID
                    String idList = updateItems.stream()
                            .map(item -> String.valueOf(item.id))
                            .collect(Collectors.joining(", "));
                    description = String.format(
                        "用户 [%s] 批量修改了 %d 条记录（ID: %s），变更内容：%s",
                        operator, updatedCount, idList, changeSummary.toString()
                    );
                } else {
                    // 大量：只显示数量
                    description = String.format(
                        "用户 [%s] 批量修改了 %d 条亚马逊记录，变更内容：%s",
                        operator, updatedCount, changeSummary.toString()
                    );
                }

                // 构建结构化日志数据
                Map<String, Object> logDetails = new HashMap<>();
                logDetails.put("ids", updateItems.stream().map(i -> i.id).collect(Collectors.toList()));
                logDetails.put("updateSeller", shouldUpdateSeller);
                logDetails.put("newSeller", req.seller);
                logDetails.put("updateLifecl", shouldUpdateLifecl);
                logDetails.put("newLifecl", req.lifecl);
                logDetails.put("updatedCount", updatedCount);
                logDetails.put("updateUserId", userId);

                // 记录日志
                OperationLogger.log(
                    request,
                    operator,
                    userId,
                    "UPDATE_BATCH",
                    "BATCH_" + System.currentTimeMillis(), // 伪 target_id
                    description,
                    logDetails
                );

                out.print(gson.toJson(new Result(true, "成功修改 " + updatedCount + " 条记录")));
            } else {
                out.print(gson.toJson(new Result(false, "批量更新失败")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            out.print(gson.toJson(new Result(false, "服务器错误: " + e.getMessage())));
        }

        System.out.println("=== BatchUpdateAmazonDataServlet 执行结束 ===");
    }

    // ===== 辅助方法：格式化显示值 =====
    private static String formatValue(String value) {
        return (value == null || value.trim().isEmpty()) ? "(空)" : value;
    }

    // ===== 内部类 =====
    static class BatchUpdateRequest {
        List<Integer> ids;
        String seller;
        String lifecl;
    }

    static class Result {
        boolean success;
        String message;
        Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}
