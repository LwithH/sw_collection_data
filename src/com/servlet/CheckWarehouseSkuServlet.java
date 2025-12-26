package com.servlet;

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

@WebServlet("/CheckWarehouseSkuServlet")
public class CheckWarehouseSkuServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        String warehouseSku = request.getParameter("warehouseSku");
        boolean exists = false;

        if (warehouseSku == null || warehouseSku.trim().isEmpty()) {
            out.print("{\"exists\":false, \"error\":\"无效的仓库SKU参数\"}");
            return;
        }

        String sql = "SELECT COUNT(*) AS count FROM warehouse_sku WHERE warehouse_sku = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, warehouseSku.trim());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    exists = rs.getInt("count") > 0;
                }
                out.print(String.format("{\"exists\":%b}", exists));
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.print("{\"exists\":false, \"error\":\"数据库查询失败\"}");
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
