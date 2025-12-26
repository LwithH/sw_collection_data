package com.dao;

import com.model.User;
import com.util.DBUtil;
import com.util.MD5Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public User login(String username, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            // ✅ 修复：添加 is_admin, is_download_admin 到 SELECT
            String sql = "SELECT id, username, is_see_amazon, is_see_account, is_see_temu, " +
                    "is_see_fulai, is_see_ad, is_see_dsp, " + // 新增这一列
                    "user_depart, user_id_ding, " +
                    "is_admin, is_download_admin " +
                    "FROM user WHERE username = ? AND password = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, MD5Util.encrypt(password));

            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setIsSeeAmazon(rs.getString("is_see_amazon"));
                user.setIsSeeAccount(rs.getString("is_see_account"));
                user.setIsSeeTemu(rs.getString("is_see_temu"));
                user.setIsSeeFulai(rs.getString("is_see_fulai"));
                user.setIsSeeAd(rs.getString("is_see_ad"));
                user.setUserDepart(rs.getString("user_depart"));
                user.setUserIdDing(rs.getString("user_id_ding"));
                user.setIsSeeDsp(rs.getString("is_see_dsp"));
                // ✅ 关键：设置管理员权限字段
                user.setIsAdmin(rs.getString("is_admin"));
                user.setIsDownloadAdmin(rs.getString("is_download_admin"));
                
                return user;
            }
            return null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            closeResources(rs, pstmt, conn);
        }
    }

    // 同样修复 getUserById（如果你在其他地方用到）
    public User getUserById(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, username, is_see_amazon, is_see_account, is_see_temu, " +
                    "is_see_fulai, is_see_ad, is_see_dsp, " + // 新增这一列
                    "user_depart, user_id_ding, " +
                    "is_admin, is_download_admin " +
                    "FROM user WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setIsSeeAmazon(rs.getString("is_see_amazon"));
                user.setIsSeeAccount(rs.getString("is_see_account"));
                user.setIsSeeTemu(rs.getString("is_see_temu"));
                user.setIsSeeFulai(rs.getString("is_see_fulai"));
                user.setIsSeeAd(rs.getString("is_see_ad"));
                user.setUserDepart(rs.getString("user_depart"));
                user.setUserIdDing(rs.getString("user_id_ding"));
                user.setIsAdmin(rs.getString("is_admin"));          // ← 设置
                user.setIsDownloadAdmin(rs.getString("is_download_admin")); // ← 设置
                user.setIsSeeDsp(rs.getString("is_see_dsp"));
                return user;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return null;
    }

    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        if (rs != null) {
            try { rs.close(); } catch (SQLException e) { /* ignore */ }
        }
        if (pstmt != null) {
            try { pstmt.close(); } catch (SQLException e) { /* ignore */ }
        }
        DBUtil.closeConnection(conn);
    }
}
