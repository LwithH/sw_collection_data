package com.util;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;

public class OperationLogger {

    public static void log(HttpServletRequest request,
                          String operator,
                          String operationType,
                          String targetId,
                          Map<String, Object> details) {
        String sql = "INSERT INTO amazon_operation_log (operator, operation_type, target_id, details, ip_address) VALUES (?, ?, ?, ?, ?)";

        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection(); // 👈 复用你的 DBUtil
            ps = conn.prepareStatement(sql);

            ps.setString(1, operator);
            ps.setString(2, operationType);
            ps.setString(3, targetId);
            ps.setString(4, toJson(details));
            ps.setString(5, getClientIpAddress(request));

            ps.executeUpdate();
        } catch (Exception e) {
            // 日志失败不应影响主业务
            System.err.println("⚠️ 操作日志记录失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (ps != null) {
                try { ps.close(); } catch (SQLException ignored) {}
            }
            DBUtil.closeConnection(conn); // 👈 使用 DBUtil 的 close 方法
        }
    }

    // === 保留你原有的 toJson 和 getClientIpAddress 方法 ===
    private static String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) return "{}";
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append("\"").append(escapeJson(entry.getKey())).append("\":\"");
            sb.append(escapeJson(entry.getValue() != null ? entry.getValue().toString() : ""));
            sb.append("\"");
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    private static String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }

    private static String getClientIpAddress(HttpServletRequest request) {
        String xip = request.getHeader("X-Real-IP");
        if (xip != null && !xip.isEmpty() && !"unknown".equalsIgnoreCase(xip)) {
            return xip;
        }
        String xfor = request.getHeader("X-Forwarded-For");
        if (xfor != null && !xfor.isEmpty()) {
            int index = xfor.indexOf(",");
            if (index != -1) {
                return xfor.substring(0, index);
            } else {
                return xfor;
            }
        }
        return request.getRemoteAddr();
    }
 // ===== 新增方法：带中文描述 =====
 // 新增 user_id 参数
    public static void log(HttpServletRequest request,
            String operator,
            Integer userId,
            String operationType,
            String targetId,
            String description,
            Map<String, Object> details) {
// 获取 User-Agent
String userAgent = request.getHeader("User-Agent");
if (userAgent == null) userAgent = "";

String sql = "INSERT INTO amazon_operation_log (operator, user_id, operation_type, target_id, description, details, ip_address, user_agent) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

Connection conn = null;
PreparedStatement ps = null;
try {
conn = DBUtil.getConnection();
ps = conn.prepareStatement(sql);

ps.setString(1, operator);
ps.setObject(2, userId);
ps.setString(3, operationType);
ps.setString(4, targetId);
ps.setString(5, description);
ps.setString(6, toJson(details));
ps.setString(7, getClientIpAddress(request));
ps.setString(8, userAgent); // ← 记录 User-Agent

ps.executeUpdate();
} catch (Exception e) {
System.err.println("⚠️ 操作日志记录失败: " + e.getMessage());
e.printStackTrace();
} finally {
if (ps != null) {
  try { ps.close(); } catch (SQLException ignored) {}
}
DBUtil.closeConnection(conn);
}
}



}
