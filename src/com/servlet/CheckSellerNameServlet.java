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
@WebServlet("/CheckSellerNameServlet")
public class CheckSellerNameServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sellerName = request.getParameter("sellerName");
        boolean exists = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT 1 FROM sellername WHERE seller_name = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, sellerName);
            rs = stmt.executeQuery();
            exists = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
        }
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        out.print("{\"exists\":" + exists + "}");
        out.flush();
    }
}
