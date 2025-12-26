package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.model.OperationLog;
import com.util.DBUtil;

/**
 * 操作日志数据访问对象（DAO）
 */
public class LogDAO {

    /**
     * 记录操作日志方法（包含时间设置，解决时区问题）
     *
     * @param log 日志对象（需包含createTime字段）
     */
    public void logOperation(OperationLog log) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            // 新增create_time字段，由Java传入时间而非依赖数据库
            String sql = "INSERT INTO operation_log " +
                         "(operation_type, operation_content, ip_address, username, create_time) " +
                         "VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, log.getOperationType());
            pstmt.setString(2, log.getOperationContent());
            pstmt.setString(3, log.getIpAddress());
            pstmt.setString(4, log.getUsername());
            // 传入Java生成的LocalDateTime时间（已处理时区）
            pstmt.setObject(5, log.getCreateTime());

            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 根据数据 ID 查询相关操作日志（精确匹配，避免匹配包含目标数字的其他ID）
     *
     * @param dataId 要查询的数据 ID
     * @return 包含该数据所有修改日志的列表（按时间倒序）
     */
    public List<OperationLog> getLogsByDataId(int dataId) {
        List<OperationLog> logs = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();

            // 核心优化：使用正则表达式匹配"ID:dataId"的完整格式，避免部分匹配
            // 正则说明：
            // - "ID:" + dataId：精确匹配"ID:目标数字"
            // - (\\D|$)：确保目标数字后面要么是非数字字符（如逗号、空格），要么是字符串结尾
            String sql = "SELECT * FROM operation_log " +
                         "WHERE operation_content REGEXP ? " +  // 正则匹配（需数据库支持，如MySQL）
                         "ORDER BY create_time DESC";
            System.out.println("正在查询 ID=" + dataId + " 的日志，SQL: " + sql);
            pstmt = conn.prepareStatement(sql);
            
            // 拼接正则表达式：匹配"ID:1"且后面不是数字（避免匹配ID:1000）
            String regex = "ID:" + dataId + "(\\D|$)";
            pstmt.setString(1, regex);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                OperationLog log = new OperationLog();
                log.setId(rs.getInt("id"));
                log.setOperationType(rs.getString("operation_type"));
                log.setOperationContent(rs.getString("operation_content"));
                log.setIpAddress(rs.getString("ip_address"));
                log.setUsername(rs.getString("username"));
                // 将数据库时间转换为LocalDateTime（保持时区一致性）
                log.setCreateTime(rs.getTimestamp("create_time").toLocalDateTime());
                logs.add(log);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
      
        return logs;
    }

}
