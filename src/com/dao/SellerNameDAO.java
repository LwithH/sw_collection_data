package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.util.DBUtil;

public class SellerNameDAO {
    // 初始化日志对象（用于打印调试信息）
    private static final Logger logger = Logger.getLogger(SellerNameDAO.class.getName());

    /**
     * 验证Seller是否存在（仅检查存在性）
     */
    public boolean isValidSeller(String sellerName) {
        String trimmedSeller = sellerName == null ? "" : sellerName.trim();
        logger.info("开始校验seller：" + trimmedSeller);

        if (trimmedSeller.isEmpty()) {
            logger.warning("校验失败：seller为空");
            return false;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                logger.severe("数据库连接失败！");
                return false;
            }

            String sql = "SELECT 1 FROM sellername WHERE seller_name = ? AND status = 0 LIMIT 1";
            logger.info("执行SQL：" + sql + "，参数：" + trimmedSeller);

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, trimmedSeller);
            rs = pstmt.executeQuery();

            boolean exists = rs.next();
            logger.info("校验结果：" + (exists ? "存在" : "不存在"));
            return exists;

        } catch (SQLException e) {
            logger.severe("校验失败，数据库异常：" + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                logger.warning("关闭资源异常：" + e.getMessage());
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    /**
     * 验证Seller是否存在并获取user_id_ding
     * @return 返回对应的user_id_ding字符串，如果不存在或查询失败返回null
     */
    public String validateSellerAndGetUserIdDing(String sellerName) {
        String trimmedSeller = sellerName == null ? "" : sellerName.trim();
        logger.info("开始校验seller并获取user_id_ding：" + trimmedSeller);

        if (trimmedSeller.isEmpty()) {
            logger.warning("校验失败：seller为空");
            return null;
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            if (conn == null) {
                logger.severe("数据库连接失败！");
                return null;
            }

            String sql = "SELECT user_id_ding FROM sellername WHERE seller_name = ? AND status = 0";
            logger.info("执行SQL：" + sql + "，参数：" + trimmedSeller);

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, trimmedSeller);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                String userIdDing = rs.getString("user_id_ding");
                logger.info("查询结果：user_id_ding = " + userIdDing);
                return userIdDing;
            } else {
                logger.info("查询结果：无匹配记录");
                return null;
            }

        } catch (SQLException e) {
            logger.severe("查询失败，数据库异常：" + e.getMessage());
            e.printStackTrace();
            return null;

        } finally {
            try {
                if (rs != null) rs.close();
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                logger.warning("关闭资源异常：" + e.getMessage());
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }
    public String getSalesDepartBySellerName(String sellerName) {
        String sql = "SELECT sales_depart FROM sellername WHERE seller_name = ? AND status = '0'";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sellerName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("sales_depart");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("获取sales_depart时出错: " + e.getMessage());
        }
        return null;
    }
    /**
     * 判断 seller_name 是否存在多个有效绑定（status=0）
     */
    public boolean isSellerDuplicated(String sellerName) {
        if (sellerName == null || sellerName.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM sellername WHERE seller_name = ? AND status = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sellerName.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 1;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获取该 seller_name 下所有有效的 user_id_ding（用于重名权限校验）
     */
    public List<String> getAllUserIdDingsBySeller(String sellerName) {
        List<String> list = new ArrayList<>();
        if (sellerName == null || sellerName.trim().isEmpty()) return list;

        String sql = "SELECT user_id_ding FROM sellername WHERE seller_name = ? AND status = 0";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sellerName.trim());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String uid = rs.getString("user_id_ding");
                    if (uid != null && !uid.isEmpty()) {
                        list.add(uid);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
 // SellerNameDAO.java
    public String getSalesDepartByUserIdDing(String userIdDing) {
        if (userIdDing == null || userIdDing.trim().isEmpty()) return null;
        String sql = "SELECT sales_depart FROM sellername WHERE user_id_ding = ? AND status = 0 LIMIT 1";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userIdDing.trim());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("sales_depart");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
