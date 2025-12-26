package com.servlet;

import com.util.DBUtil; // 修正：导入正确的 DBUtil 包（com.util 而非 com.dao）
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/GetSitesByAreaServlet")
public class GetSitesByAreaServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        String areaIdParam = request.getParameter("areaId");
        String platformIdParam = request.getParameter("platformId");

        // 参数校验
        if (areaIdParam == null || areaIdParam.trim().isEmpty()) {
            sendError(response, "缺少 areaId 参数");
            return;	
        }

        int areaId, platformId;
        try {
            areaId = Integer.parseInt(areaIdParam);
            platformId = (platformIdParam != null) ? Integer.parseInt(platformIdParam) : 3;
        } catch (NumberFormatException e) {
            sendError(response, "参数格式错误：areaId/platformId 必须是整数");
            return;
        }

        List<Map<String, Object>> sites = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection(); // 现在能正确调用 DBUtil 方法
            // SQL 语句：根据实际表结构调整字段名（确保 gsite 表存在且字段正确）
            String sql = "SELECT site_id, site FROM gsite WHERE area_id = ? AND platform_id = ?";

            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, areaId);
            pstmt.setInt(2, platformId);  // 第2个 ?
            rs = pstmt.executeQuery();
            while (rs.next()) {
                Map<String, Object> site = new HashMap<>();
                site.put("site_id", rs.getInt("site_id"));
                site.put("site", rs.getString("site"));
                sites.add(site);
            }
            
            // 返回 JSON 数组
            new Gson().toJson(sites, response.getWriter());
            
        } catch (RuntimeException e) {
            // 捕获驱动加载失败的异常
            e.printStackTrace();
            sendError(response, "数据库驱动异常: " + e.getMessage());
        } catch (Exception e) {
            // 捕获数据库连接/查询异常
            e.printStackTrace();
            sendError(response, "查询站点失败: " + e.getMessage());
        } finally {
            // 统一关闭所有资源（替代原有的零散关闭）
        	 DBUtil.closeConnection(conn);
        }
    }

    private void sendError(HttpServletResponse response, String message) throws IOException {
        JsonObject error = new JsonObject();
        error.addProperty("success", false);
        error.addProperty("message", message);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.getWriter().write(new Gson().toJson(error));
    }
}