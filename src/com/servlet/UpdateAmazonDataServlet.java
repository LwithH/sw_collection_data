package com.servlet;

import com.dao.AmazonDataDAO;
import com.dao.SellerNameDAO;
import com.google.gson.Gson;
import com.model.AmazonData;
import com.model.User;
import com.util.OperationLogger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@WebServlet("/UpdateAmazonDataServlet")
public class UpdateAmazonDataServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        System.out.println("=== UpdateAmazonDataServlet 开始执行 ===");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");
        if (user == null || !"yes".equalsIgnoreCase(user.getIsSeeAmazon())) {
            System.out.println("用户无权限操作");
            out.print(gson.toJson(new Result(false, "无权操作")));
            return;
        }

        try {
            String json = request.getReader().lines().reduce("", (a, b) -> a + b);
            System.out.println("接收到的JSON数据: " + json);

            UpdateRequest req = gson.fromJson(json, UpdateRequest.class);
            System.out.println("解析后的请求对象: id=" + req.id + ", seller=" + req.seller + ", lifecl=" + req.lifecl);

            if (req.id <= 0) {
                System.out.println("无效ID: " + req.id);
                out.print(gson.toJson(new Result(false, "无效ID")));
                return;
            }

            String userIdDing = null;
            SellerNameDAO sellerDao = new SellerNameDAO();

            if (req.seller != null && !req.seller.trim().isEmpty()) {
                System.out.println("开始校验seller: " + req.seller);

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
                    userIdDing = currentUserDingId;
                } else {
                    userIdDing = sellerDao.validateSellerAndGetUserIdDing(req.seller);
                }
            }

            String salesDepart = "0";
            if (userIdDing != null) {
                salesDepart = sellerDao.getSalesDepartByUserIdDing(userIdDing);
                if (salesDepart == null) {
                    salesDepart = "0";
                }
            } else {
                salesDepart = "0";
            }

            AmazonDataDAO dataDao = new AmazonDataDAO();
            AmazonData originalData = dataDao.getOriginalData(req.id);
            if (originalData == null) {
                System.out.println("数据不存在: " + req.id);
                out.print(gson.toJson(new Result(false, "数据不存在")));
                return;
            }

            String originalSeller = originalData.getSeller();
            String isc1Value = null;

            if (originalSeller == null || originalSeller.trim().isEmpty() || "-".equals(originalSeller)) {
                if (req.seller != null && !req.seller.trim().isEmpty() && !"-".equals(req.seller)) {
                    isc1Value = "首位";
                }
            } else {
                if (req.seller != null && !req.seller.equals(originalSeller)) {
                    isc1Value = "接手";
                    System.out.println("seller变化: 从" + originalSeller + "变为" + req.seller + ", isc1设置为接手");
                } else if ((req.seller == null || req.seller.trim().isEmpty()) &&
                        (originalSeller != null && !originalSeller.trim().isEmpty() && !"-".equals(originalSeller))) {
                    System.out.println("尝试清空已分配的seller，拒绝操作");
                    out.print(gson.toJson(new Result(false, "不能清空已分配的 Seller")));
                    return;
                } else {
                    isc1Value = originalData.getIsc1();
                }
            }

            if (isc1Value == null) {
                isc1Value = originalData.getIsc1();
            }

            int updateUserId = user.getId();

            boolean success = dataDao.updateSellerLifeclIsc1UserIdDingAndSalesDepart(
                    req.id, req.seller, req.lifecl, isc1Value, userIdDing, salesDepart, updateUserId);

            System.out.println("更新结果: " + success);

            if (success) {
                // ========== 生成中文日志描述 ==========
                String operator = user.getUsername(); // 确保 User 有 getUsername()
                StringBuilder desc = new StringBuilder();
                desc.append("用户 [").append(operator).append("] 修改了亚马逊记录 ID=").append(req.id);

                List<String> changes = new ArrayList<>();

                // Seller 变更
                if (!Objects.equals(originalData.getSeller(), req.seller)) {
                    changes.add(String.format("Seller: %s → %s",
                            formatValue(originalData.getSeller()),
                            formatValue(req.seller)));
                }

                // Lifecl 变更
                if (!Objects.equals(originalData.getLifecl(), req.lifecl)) {
                    changes.add(String.format("Lifecl: %s → %s",
                            formatValue(originalData.getLifecl()),
                            formatValue(req.lifecl)));
                }

                // Isc1 变更
                if (!Objects.equals(originalData.getIsc1(), isc1Value)) {
                    changes.add(String.format("ISC1: %s → %s",
                            formatValue(originalData.getIsc1()),
                            formatValue(isc1Value)));
                }

                // SalesDepart 变更
                if (!Objects.equals(originalData.getSalesDepart(), salesDepart)) {
                    changes.add(String.format("部门: %s → %s",
                            formatDepartName(originalData.getSalesDepart()),
                            formatDepartName(salesDepart)));
                }

                if (!changes.isEmpty()) {
                    desc.append("，变更内容：").append(String.join("；", changes));
                } else {
                    desc.append("（无实际字段变更）");
                }

                // ========== 构建结构化日志数据 ==========
                Map<String, Object> logDetails = new HashMap<>();
                logDetails.put("id", req.id);
                logDetails.put("sku", originalData.getSku()); // 补充 SKU 便于追踪
                logDetails.put("oldSeller", originalData.getSeller());
                logDetails.put("newSeller", req.seller);
                logDetails.put("oldLifecl", originalData.getLifecl());
                logDetails.put("newLifecl", req.lifecl);
                logDetails.put("oldIsc1", originalData.getIsc1());
                logDetails.put("newIsc1", isc1Value);
                logDetails.put("oldSalesDepart", originalData.getSalesDepart());
                logDetails.put("newSalesDepart", salesDepart);
                logDetails.put("userIdDing", userIdDing);
                logDetails.put("updateUserId", updateUserId);

                // ========== 记录日志 ==========
             // ========== 记录日志 ==========
                OperationLogger.log(
                    request,
                    operator,
                    user.getId(), // ← 直接传入用户ID
                    "UPDATE_SINGLE",
                    String.valueOf(req.id),
                    desc.toString(),
                    logDetails
                );


                out.print(gson.toJson(new Result(true, "修改成功")));
            } else {
                out.print(gson.toJson(new Result(false, "更新失败")));
            }
        } catch (Exception e) {
            System.out.println("发生异常: " + e.getMessage());
            e.printStackTrace();
            out.print(gson.toJson(new Result(false, "服务器错误")));
        }
        System.out.println("=== UpdateAmazonDataServlet 执行结束 ===");
    }

    // ===== 辅助方法：格式化显示值 =====
    private static String formatValue(String value) {
        if (value == null || value.trim().isEmpty() || "-".equals(value)) {
            return "(空)";
        }
        return value;
    }

    // ===== 辅助方法：部门代码转中文 =====
    private static String formatDepartName(String code) {
        if (code == null || "0".equals(code) || code.trim().isEmpty()) {
            return "未分配";
        }
        try {
            int c = Integer.parseInt(code);
            switch (c) {
                case 1: return "销售一部";
                case 2: return "销售二部";
                case 3: return "乐器项目部";
                case 4: return "大件项目部";
                case 5: return "销售三部";
                case 6: return "天津项目部";
                case 7: return "工业项目部";
                case 97: return "Ali";
                case 98: return "深圳公司";
                case 99: return "停用";
                default: return "未知部门(" + code + ")";
            }
        } catch (NumberFormatException e) {
            return "未知部门(" + code + ")";
        }
    }

    // ===== 内部类 =====
    static class UpdateRequest {
        int id;
        String seller;
        String lifecl;

        @Override
        public String toString() {
            return "UpdateRequest{id=" + id + ", seller='" + seller + "', lifecl='" + lifecl + "'}";
        }
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
