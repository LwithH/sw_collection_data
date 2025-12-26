// com.dao.V3DAO
package com.dao;//账号系统修改部门

import com.model.V3;
import com.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class V3DAO {
    public List<V3> getAllDepartments() {
        List<V3> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT sales_depart, depart_name FROM v3 WHERE sales_depart IS NOT NULL ORDER BY sales_depart";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            while (rs.next()) {
                V3 v = new V3();
                v.setSalesDepart(rs.getInt("sales_depart"));
                v.setDepartName(rs.getString("depart_name"));
                list.add(v);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
        }
        return list;
    }
 // V3DAO.java
    public String getDepartNameById(int salesDepartId) {
        String departName = "未知部门";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT depart_name FROM v3 WHERE sales_depart = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, salesDepartId);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                departName = rs.getString("depart_name");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return departName;
    }

}
