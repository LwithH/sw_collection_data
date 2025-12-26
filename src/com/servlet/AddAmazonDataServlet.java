package com.servlet;

import com.dao.*;
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
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/AddAmazonDataServlet")
public class AddAmazonDataServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Gson gson = new Gson();

        System.out.println("=== AddAmazonDataServlet 开始执行 ===");

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

            AddRequest req = gson.fromJson(json, AddRequest.class);
            System.out.println("解析后的请求对象: uacs=" + req.uacs + ", sku=" + req.sku + ", seller=" + req.seller);

            if (req.uacs == null || req.uacs.isEmpty()) {
                out.print(gson.toJson(new Result(false, "账户(UACS)不能为空")));
                return;
            }
            if (req.sku == null || req.sku.isEmpty()) {
                out.print(gson.toJson(new Result(false, "SKU不能为空")));
                return;
            }
            if (req.seller == null || req.seller.isEmpty()) {
                out.print(gson.toJson(new Result(false, "Seller不能为空")));
                return;
            }

            AccountDAO accountDAO = new AccountDAO();
            AccountDAO.MarketInfo marketInfo = accountDAO.getMarketInfoByUacs(req.uacs);
            if (marketInfo == null) {
                out.print(gson.toJson(new Result(false, "无效的账户(UACS)")));
                return;
            }
            System.out.println("获取到market_id: " + marketInfo.marketId + ", uac_id: " + marketInfo.uacId);

            SellerNameDAO sellerDao = new SellerNameDAO();
            if (!sellerDao.isValidSeller(req.seller)) {
                out.print(gson.toJson(new Result(false, "Seller 不存在或已停用")));
                return;
            }

            String userIdDing;
            if (sellerDao.isSellerDuplicated(req.seller)) {
                List<String> allowedDingIds = sellerDao.getAllUserIdDingsBySeller(req.seller);
                String currentUserDingId = user.getUserIdDing();
                if (currentUserDingId == null || !allowedDingIds.contains(currentUserDingId)) {
                    out.print(gson.toJson(new Result(false, "❌ 您无权为他人负责的重名 Seller [" + req.seller + "] 新增数据")));
                    return;
                }
                userIdDing = currentUserDingId;
            } else {
                userIdDing = sellerDao.validateSellerAndGetUserIdDing(req.seller);
                if (userIdDing == null) {
                    out.print(gson.toJson(new Result(false, "Seller 未关联有效用户")));
                    return;
                }
            }

            String salesDepart = sellerDao.getSalesDepartByUserIdDing(userIdDing);
            if (salesDepart == null) {
                salesDepart = "0";
            }

            AmazonData newData = new AmazonData();
            newData.setUacs(req.uacs);
            newData.setSku(req.sku);
            newData.setSeller(req.seller);
            newData.setWarehouseSku(req.warehouseSku);
            newData.setAsin(req.asin);
            newData.setParentAsin(req.parentAsin);
            newData.setLifecl(req.lifecl);
            newData.setSalesDepart(salesDepart);
            newData.setMarketId(marketInfo.marketId);
            newData.setUacId(marketInfo.uacId);
            newData.setIsc1("首位");
            newData.setUser_id_ding(userIdDing);
            newData.setSkuLastDate(Date.valueOf(LocalDate.now().minusDays(1)));
            newData.setCreateUserId(user.getId());
            newData.setUpdateUserId(user.getId());

            AmazonDataDAO dataDAO = new AmazonDataDAO();
            boolean success = dataDAO.insertAmazonData(newData);

            if (success) {
                System.out.println("新增记录成功: UACS=" + req.uacs + ", SKU=" + req.sku);

                // ========== 【新增】记录操作日志 ==========
                String operator = user.getUsername(); // 或 getName()
                Integer userId = user.getId();

                // 生成中文描述
                String description = String.format(
                    "用户 [%s] 新增了亚马逊记录：账户=%s, SKU=%s, Seller=%s, Lifecl=%s",
                    operator,
                    formatValue(req.uacs),
                    formatValue(req.sku),
                    formatValue(req.seller),
                    formatValue(req.lifecl)
                );

                // 构建结构化日志数据
                Map<String, Object> logDetails = new HashMap<>();
                logDetails.put("uacs", req.uacs);
                logDetails.put("sku", req.sku);
                logDetails.put("seller", req.seller);
                logDetails.put("warehouseSku", req.warehouseSku);
                logDetails.put("asin", req.asin);
                logDetails.put("parentAsin", req.parentAsin);
                logDetails.put("lifecl", req.lifecl);
                logDetails.put("salesDepart", salesDepart);
                logDetails.put("userIdDing", userIdDing);
                logDetails.put("createUserId", userId);

                // 记录日志（自动包含 user_agent 和 IP）
                OperationLogger.log(
                    request,
                    operator,
                    userId,
                    "ADD_SINGLE",
                    null, // 新增记录无 target_id，可存 null
                    description,
                    logDetails
                );

                out.print(gson.toJson(new Result(true, "新增成功")));
            } else {
                out.print(gson.toJson(new Result(false, "保存失败，请重试")));
            }
        } catch (Exception e) {
            System.out.println("发生异常: " + e.getMessage());
            e.printStackTrace();
            out.print(gson.toJson(new Result(false, "服务器错误: " + e.getMessage())));
        }
        System.out.println("=== AddAmazonDataServlet 执行结束 ===");
    }

    // ===== 辅助方法：格式化显示值（避免 null 显示）=====
    private static String formatValue(String value) {
        return (value == null || value.trim().isEmpty()) ? "(空)" : value;
    }

    // ===== 内部类 =====
    static class AddRequest {
        String uacs;
        String sku;
        String seller;
        String warehouseSku;
        String asin;
        String parentAsin;
        String lifecl;

        @Override
        public String toString() {
            return "AddRequest{uacs='" + uacs + "', sku='" + sku + "', seller='" + seller + "'}";
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
