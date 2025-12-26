package com.servlet;
//品牌广告用到的检验sku
import com.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
@WebServlet("/CheckSkuServlet")
public class CheckSkuServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        
        String sku = request.getParameter("sku");
        boolean exists = false;
        
        if (sku == null || sku.trim().isEmpty()) {
            out.print("{\"exists\":false, \"error\":\"无效的SKU参数\"}");
            return;
        }
        
        String trimmedSku = sku.trim();
        Connection conn = null;
        PreparedStatement pstmt1 = null, pstmt2 = null;
        ResultSet rs1 = null, rs2 = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // 1. 检查 amazon_data 表
            String sql1 = "SELECT 1 FROM amazon_data WHERE sku = ? LIMIT 1";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setString(1, trimmedSku);
            rs1 = pstmt1.executeQuery();
            if (rs1.next()) {
                exists = true; // 在亚马逊库中找到
            } else {
                // 2. 未在 amazon_data 中找到，再检查 brand_ad_map 表
                String sql2 = "SELECT 1 FROM brand_ad_map WHERE platform_sku = ? LIMIT 1";
                pstmt2 = conn.prepareStatement(sql2);
                pstmt2.setString(1, trimmedSku);
                rs2 = pstmt2.executeQuery();
                if (rs2.next()) {
                    exists = true; // 在 brand_ad_map 中找到
                }
            }
            
            out.print(String.format("{\"exists\":%b}", exists));
            
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"exists\":false, \"error\":\"数据库查询失败\"}");
        } finally {
            // 关闭所有资源
            try { if (rs1 != null) rs1.close(); } catch (SQLException e) { /* ignore */ }
            try { if (pstmt1 != null) pstmt1.close(); } catch (SQLException e) { /* ignore */ }
            try { if (rs2 != null) rs2.close(); } catch (SQLException e) { /* ignore */ }
            try { if (pstmt2 != null) pstmt2.close(); } catch (SQLException e) { /* ignore */ }
            DBUtil.closeConnection(conn);
        }
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}

