package com.dao;

import com.model.AccountData;
import com.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AccountDAO {
    
    public List<AccountData> getPaginatedAccountData(int page, int size) {
        List<AccountData> accountDataList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            // 核心修改：gsite关联条件改为a.country_id = g.site_id（因a.country_id实际存的是site_id）
            String sql = "SELECT " +
                    "a.id, a.mains, a.acc_name, a.type_opid, a.country_id, " +
                    "a.platformid, a.sales_depart, a.status, a.area_id, " +
                    "a.ziniao, a.receipt_status, " +  // ✅ 新增字段：ziniao和receipt_status
                    "t.type_op, g.site, p.platform, v.depart_name, ar.area " +
                    "FROM account_data a " +
                    "LEFT JOIN account_data_top t ON a.type_opid = t.type_opid " +
                    "LEFT JOIN gsite g ON a.country_id = g.site_id " +
                    "LEFT JOIN platform p ON a.platformid = p.platformid " +
                    "LEFT JOIN v3 v ON a.sales_depart = v.sales_depart " +
                    "LEFT JOIN garea ar ON a.area_id = ar.area_id " +
                    "GROUP BY " +
                    "a.id, a.mains, a.acc_name, a.type_opid, a.country_id, " +
                    "a.platformid, a.sales_depart, a.status, a.area_id, " +
                    "a.ziniao, a.receipt_status, " +  // ✅ 同步新增到GROUP BY子句
                    "t.type_op, g.site, p.platform, v.depart_name, ar.area " +
                    "LIMIT ? OFFSET ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, size);
            pstmt.setInt(2, (page - 1) * size);
            
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                AccountData account = new AccountData();
                account.setId(rs.getInt("id"));
                account.setMains(rs.getString("mains"));
                account.setAccName(rs.getString("acc_name"));
                account.setTypeOpid(rs.getInt("type_opid"));
                account.setCountryId(rs.getInt("country_id"));  // 实际为gsite.site_id
                account.setPlatformid(rs.getInt("platformid"));
                account.setSalesDepart(rs.getInt("sales_depart"));
                account.setStatus(rs.getString("status"));
                account.setAreaId(rs.getInt("area_id"));
                
                // 设置关联字段（此时g.site为正确的国家/站点名称）
                account.setTypeOp(rs.getString("type_op"));
                account.setCountry(rs.getString("site"));  // 关联后获取正确的site值
                account.setPlatform(rs.getString("platform"));
                account.setDepartName(rs.getString("depart_name"));
                account.setArea(rs.getString("area"));
               
                accountDataList.add(account);
                account.setZiniao(rs.getString("ziniao"));
                account.setReceiptStatus(rs.getString("receipt_status"));

            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            // 补充关闭pstmt和rs（避免资源泄露）
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return accountDataList;
    }

    public void updateSalesDepart(int accountId, int salesDepart) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE account_data SET sales_depart = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, salesDepart);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public int getTotalAccountCount() {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            // 同步修改gsite关联条件，确保统计范围与分页查询一致
            String sql = "SELECT COUNT(DISTINCT a.id) AS total_count " +
                         "FROM account_data a " +
                         "LEFT JOIN account_data_top t ON a.type_opid = t.type_opid " +
                         "LEFT JOIN gsite g ON a.country_id = g.site_id " +  // 修正关联条件
                         "LEFT JOIN platform p ON a.platformid = p.platformid " +
                         "LEFT JOIN v3 v ON a.sales_depart = v.sales_depart " +
                         "LEFT JOIN garea ar ON a.area_id = ar.area_id";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                count = rs.getInt("total_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return count;
    }
    
    public int getActiveAccountsCount() {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(*) AS active_count " +
                         "FROM account_data " +
                         "WHERE status = '1'";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                count = rs.getInt("active_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return count;
    }
    
    public int getDistinctPlatformCount() {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(DISTINCT platformid) AS platform_count " +
                         "FROM account_data " +
                         "WHERE platformid IS NOT NULL";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                count = rs.getInt("platform_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return count;
    }
   
    // 修正：统计国家总数（需关联gsite表，按g.country_id去重，而非a.country_id）
    public int getDistinctCountryCount() {
        int count = 0;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            // 关联gsite，统计g.country_id的去重数量（实际国家ID）
            String sql = "SELECT COUNT(DISTINCT g.country_id) AS country_count " +
                         "FROM account_data a " +
                         "LEFT JOIN gsite g ON a.country_id = g.site_id " +  // 修正关联条件
                         "WHERE a.country_id IS NOT NULL AND g.country_id IS NOT NULL";
            
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                count = rs.getInt("country_count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        
        return count;
    }

    public List<AccountData> getAllAccountData() {
        return getPaginatedAccountData(1, Integer.MAX_VALUE);
    }
    
    public Map<String, String> getSystemFieldsById(int id) {
        java.util.Map<String, String> map = new java.util.HashMap<>();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT s1,s2,s3,s4,s5,s6,s7,s8,s9,s10," +
                         "s11,s12,s13,s14,s15,s16,s17,s18,s19,s20 " +
                         "FROM account_data WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                for (int i = 1; i <= 20; i++) {
                    map.put("s" + i, rs.getString("s" + i));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return map;
    }

    public void updateSystemFields(int accountId, String[] sValues) {
        if (sValues == null || sValues.length != 20) {
            throw new IllegalArgumentException("必须提供20个系统字段值");
        }

        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder("UPDATE account_data SET ");
            for (int i = 1; i <= 20; i++) {
                sql.append("s").append(i).append(" = ?");
                if (i < 20) sql.append(", ");
            }
            sql.append(" WHERE id = ?");

            pstmt = conn.prepareStatement(sql.toString());
            for (int i = 0; i < 20; i++) {
                pstmt.setString(i + 1, sValues[i]);
            }
            pstmt.setInt(21, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新系统字段失败", e);
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }
    
 // 获取所有模式
    public List<Map<String, Object>> getAllModes() {
        return queryDropdown("SELECT type_opid, type_op FROM account_data_top WHERE type_opid IS NOT NULL", "type_opid", "type_op");
    }

    // 获取所有站点（gsite 表）
    public List<Map<String, Object>> getAllSites() {
        return queryDropdown("SELECT site_id, site FROM gsite WHERE site_id IS NOT NULL", "site_id", "site");
    }

    // 获取所有区域
    public List<Map<String, Object>> getAllAreas() {
        return queryDropdown("SELECT area_id, area FROM garea WHERE area_id IS NOT NULL", "area_id", "area");
    }

    // 获取所有平台
    public List<Map<String, Object>> getAllPlatforms() {
        return queryDropdown("SELECT platformid, platform FROM platform WHERE platformid IS NOT NULL", "platformid", "platform");
    }

    // 通用下拉查询
    private List<Map<String, Object>> queryDropdown(String sql, String idField, String nameField) {
        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Map<String, Object> map = new HashMap<>();
                map.put(idField, rs.getObject(idField));
                map.put(nameField, rs.getString(nameField));
                list.add(map);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    public void insertAccount(AccountData account) {
        // 检查 countryId 和 areaId，如果为0则插入NULL
        String countryValue = (account.getCountryId() == 0) ? "NULL" : "?";
        String areaValue = (account.getAreaId() == 0) ? "NULL" : "?";
        
        // 动态构建SQL
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("INSERT INTO account_data (")
                  .append("mains, acc_name, type_opid, ");
        
        if (account.getCountryId() != 0) {
            sqlBuilder.append("country_id, ");
        }
        if (account.getAreaId() != 0) {
            sqlBuilder.append("area_id, ");
        }
        
        // 添加 s1 字段
        sqlBuilder.append("platformid, sales_depart, status, ziniao, s1")
                  .append(") VALUES (?, ?, ?, ");
        
        // 添加 country_id 和 area_id 占位符
        if (account.getCountryId() != 0) {
            sqlBuilder.append("?, ");
        }
        if (account.getAreaId() != 0) {
            sqlBuilder.append("?, ");
        }
        
        // 调整占位符数量以匹配字段数
        // 基础字段：mains, acc_name, type_opid (3个)
        // 可选字段：country_id, area_id (0-2个)
        // 固定字段：platformid, sales_depart, status, ziniao, s1 (5个)
        int totalPlaceholders = 3;
        if (account.getCountryId() != 0) totalPlaceholders++;
        if (account.getAreaId() != 0) totalPlaceholders++;
        totalPlaceholders += 5; // 平台、部门、状态、紫鸟、易仓名
        
        // 添加占位符
        sqlBuilder.append("?, ?, ?, ?, ?)"); // 5个占位符对应 platformid, sales_depart, status, ziniao, s1
        
        String sql = sqlBuilder.toString();
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            int paramIndex = 1;
            pstmt.setString(paramIndex++, account.getMains());
            pstmt.setString(paramIndex++, account.getAccName());
            pstmt.setInt(paramIndex++, account.getTypeOpid());
            
            // 设置 country_id（如果存在）
            if (account.getCountryId() != 0) {
                pstmt.setInt(paramIndex++, account.getCountryId());
            }
            
            // 设置 area_id（如果存在）
            if (account.getAreaId() != 0) {
                pstmt.setInt(paramIndex++, account.getAreaId());
            }
            
            pstmt.setInt(paramIndex++, account.getPlatformid());
            pstmt.setInt(paramIndex++, account.getSalesDepart());
            pstmt.setString(paramIndex++, account.getStatus());
            pstmt.setString(paramIndex++, account.getZiniao());
            pstmt.setString(paramIndex++, account.getS1()); // 设置易仓名
            
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("新增账号失败", e);
        }
    }

    /**
     * 获取所有销售部门（从 v3 表）
     */
    public List<Map<String, Object>> getAllSalesDepartments() {
        List<Map<String, Object>> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT sales_depart, depart_name FROM v3 WHERE sales_depart IS NOT NULL ORDER BY sales_depart";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> dept = new HashMap<>();
                dept.put("sales_depart", rs.getInt("sales_depart"));
                dept.put("depart_name", rs.getString("depart_name"));
                list.add(dept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return list;
    }
    /**
     * 更新收款状态
     */
    public void updateReceiptStatus(int accountId, String receiptStatus) {
        String sql = "UPDATE account_data SET receipt_status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, receiptStatus);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新收款状态失败", e);
        }
    }

    /**
     * 更新店铺状态
     */
    public void updateShopStatus(int accountId, String status) {
        String sql = "UPDATE account_data SET status = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新店铺状态失败", e);
        }
    }

 // 辅助方法：构建模糊搜索条件
    private String buildKeywordCondition(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return "";
        }
        String k = "%" + keyword.trim() + "%";
        return " AND (a.mains LIKE ? OR a.acc_name LIKE ? OR a.ziniao LIKE ? " +
               "OR t.type_op LIKE ? OR g.site LIKE ? OR ar.area LIKE ? " +
               "OR p.platform LIKE ? OR v.depart_name LIKE ?)";
    }
    public List<AccountData> searchPaginatedAccountData(
            String searchField, String keyword, 
            String receiptStatus, String shopStatus, 
            int page, int size) {
        
        List<AccountData> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT a.id, a.mains, a.acc_name, a.type_opid, a.country_id, " +
                "a.platformid, a.sales_depart, a.status, a.area_id, " +
                "a.ziniao, a.receipt_status, a.s1, " + // 新增：s1字段
                "t.type_op, g.site, p.platform, v.depart_name, ar.area " +
                "FROM account_data a " +
                "LEFT JOIN account_data_top t ON a.type_opid = t.type_opid " +
                "LEFT JOIN gsite g ON a.country_id = g.site_id " +
                "LEFT JOIN platform p ON a.platformid = p.platformid " +
                "LEFT JOIN v3 v ON a.sales_depart = v.sales_depart " +
                "LEFT JOIN garea ar ON a.area_id = ar.area_id " +
                "WHERE 1=1 "
            );

            // 将搜索字段映射为实际列（新增 s1）
            String columnExpr = mapSearchFieldToColumn(searchField);
            if (columnExpr != null && keyword != null && !keyword.trim().isEmpty()) {
                sql.append(" AND ").append(columnExpr).append(" LIKE ?");
            }

            // 筛选条件
            if (receiptStatus != null && !receiptStatus.isEmpty()) {
                sql.append(" AND a.receipt_status = ?");
            }
            if (shopStatus != null && !shopStatus.isEmpty()) {
                sql.append(" AND a.status = ?");
            }

            sql.append(" ORDER BY a.id LIMIT ? OFFSET ?");

            pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;

            // 设置搜索参数
            if (columnExpr != null && keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + keyword.trim() + "%");
            }

            // 设置筛选参数
            if (receiptStatus != null && !receiptStatus.isEmpty()) {
                pstmt.setString(paramIndex++, receiptStatus);
            }
            if (shopStatus != null && !shopStatus.isEmpty()) {
                pstmt.setString(paramIndex++, shopStatus);
            }

            // 分页
            pstmt.setInt(paramIndex++, size);
            pstmt.setInt(paramIndex++, (page - 1) * size);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                AccountData account = new AccountData();
                account.setId(rs.getInt("id"));
                account.setMains(rs.getString("mains"));
                account.setAccName(rs.getString("acc_name"));
                account.setTypeOpid(rs.getInt("type_opid"));
                account.setCountryId(rs.getInt("country_id"));
                account.setPlatformid(rs.getInt("platformid"));
                account.setSalesDepart(rs.getInt("sales_depart"));
                account.setStatus(rs.getString("status"));
                account.setAreaId(rs.getInt("area_id"));
                account.setZiniao(rs.getString("ziniao"));
                account.setReceiptStatus(rs.getString("receipt_status"));
                account.setS1(rs.getString("s1")); // 设置易仓名

                account.setTypeOp(rs.getString("type_op"));
                account.setCountry(rs.getString("site"));
                account.setPlatform(rs.getString("platform"));
                account.setDepartName(rs.getString("depart_name"));
                account.setArea(rs.getString("area"));

                list.add(account);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return list;
    }


    public int countAccountData(String searchField, String keyword, String receiptStatus, String shopStatus) {
        return countWithConditions(searchField, keyword, receiptStatus, shopStatus, "COUNT(DISTINCT a.id)");
    }

    public int countActiveAccounts(String searchField, String keyword, String receiptStatus, String shopStatus) {
        return countWithConditions(searchField, keyword, receiptStatus, shopStatus, "COUNT(CASE WHEN a.status = '1' THEN 1 END)");
    }

    public int countDistinctPlatforms(String searchField, String keyword, String receiptStatus, String shopStatus) {
        return countWithConditions(searchField, keyword, receiptStatus, shopStatus, "COUNT(DISTINCT a.platformid)");
    }

    public int countDistinctCountries(String searchField, String keyword, String receiptStatus, String shopStatus) {
        return countWithConditions(searchField, keyword, receiptStatus, shopStatus, "COUNT(DISTINCT g.country_id)");
    }


    // 通用统计方法
    private int countWithConditions(
            String searchField, String keyword, 
            String receiptStatus, String shopStatus, 
            String selectExpr) {
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DBUtil.getConnection();
            StringBuilder sql = new StringBuilder(
                "SELECT " + selectExpr + " AS cnt " +
                "FROM account_data a " +
                "LEFT JOIN account_data_top t ON a.type_opid = t.type_opid " +
                "LEFT JOIN gsite g ON a.country_id = g.site_id " +
                "LEFT JOIN platform p ON a.platformid = p.platformid " +
                "LEFT JOIN v3 v ON a.sales_depart = v.sales_depart " +
                "LEFT JOIN garea ar ON a.area_id = ar.area_id " +
                "WHERE 1=1 "
            );

            String columnExpr = mapSearchFieldToColumn(searchField);
            if (columnExpr != null && keyword != null && !keyword.trim().isEmpty()) {
                sql.append(" AND ").append(columnExpr).append(" LIKE ?");
            }
            if (receiptStatus != null && !receiptStatus.isEmpty()) {
                sql.append(" AND a.receipt_status = ?");
            }
            if (shopStatus != null && !shopStatus.isEmpty()) {
                sql.append(" AND a.status = ?");
            }

            pstmt = conn.prepareStatement(sql.toString());
            int paramIndex = 1;

            if (columnExpr != null && keyword != null && !keyword.trim().isEmpty()) {
                pstmt.setString(paramIndex++, "%" + keyword.trim() + "%");
            }
            if (receiptStatus != null && !receiptStatus.isEmpty()) {
                pstmt.setString(paramIndex++, receiptStatus);
            }
            if (shopStatus != null && !shopStatus.isEmpty()) {
                pstmt.setString(paramIndex++, shopStatus);
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt("cnt");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.closeConnection(conn);
            if (pstmt != null) try { pstmt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
        }

        return count;
    }

 // 将前端字段名映射为 SQL 中的实际表达式
    private String mapSearchFieldToColumn(String searchField) {
        if (searchField == null) return null;
        
        switch (searchField) {
            case "mains":       return "a.mains";
            case "acc_name":    return "a.acc_name";
            case "ziniao":      return "a.ziniao";
            case "s1":          return "a.s1"; // 新增：易仓名搜索
            case "type_op":     return "t.type_op";
            case "country":     return "g.site";
            case "area":        return "ar.area";
            case "platform":    return "p.platform";
            case "depart_name": return "v.depart_name";
            default:            return null;
        }
    }


    /**
     * 从g_account表获取所有UACS值（去重）
     */
    public List<String> getAllUacs() {
        List<String> uacsList = new ArrayList<>();
        String sql = "SELECT DISTINCT uacs FROM g_account WHERE uacs IS NOT NULL AND uacs != '' ORDER BY uacs";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                String uacs = rs.getString("uacs");
                uacsList.add(uacs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.err.println("获取UACS列表时发生数据库错误: " + e.getMessage());
        }
        
        return uacsList;
    }

    /**
     * 根据UACS获取market_id和uac_id
     */
    public MarketInfo getMarketInfoByUacs(String uacs) {
        String sql = "SELECT market_id, uac_id FROM g_account WHERE uacs = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, uacs);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                MarketInfo info = new MarketInfo();
                info.marketId = rs.getString("market_id");
                info.uacId = rs.getString("uac_id");
                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 内部类：存储market_id和uac_id
    public static class MarketInfo {
        public String marketId;
        public String uacId;
    }
    /**
     * 更新模式（运营类型）
     */
    public void updateTypeOp(int accountId, int typeOpid) {
        String sql = "UPDATE account_data SET type_opid = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, typeOpid);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("更新模式失败", e);
        }
    }


}