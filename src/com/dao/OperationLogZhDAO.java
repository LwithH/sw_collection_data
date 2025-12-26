// com/dao/OperationLogZhDAO.java
package com.dao;

import com.model.OperationLogZh;
import com.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OperationLogZhDAO {

    public void insertLogZh(OperationLogZh logZh) {
        String sql = "INSERT INTO operation_log_zh (user_id, username, operation_type, operation_desc, ip_address, create_time) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, logZh.getUserId());
            pstmt.setString(2, logZh.getUsername());
            pstmt.setString(3, logZh.getOperationType());
            pstmt.setString(4, logZh.getOperationDesc());
            pstmt.setString(5, logZh.getIpAddress());
            pstmt.setString(6, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("记录日志失败（Zh）", e);
        }
    }
}
