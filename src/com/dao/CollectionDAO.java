package com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.model.CollectionData;
import com.util.DBUtil;

public class CollectionDAO {

    // ========== 旧表 collection_data 操作（保持不变） ==========

    public boolean insertData(CollectionData data) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO collection_data (sku, seller, isc1, create_user_id, user_id_ding) VALUES (?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, data.getSku());
            pstmt.setString(2, data.getSeller());
            pstmt.setString(3, data.getIsc1());
            pstmt.setInt(4, data.getCreateUserId());
            pstmt.setString(5, data.getUserIdDing());
            int rows = pstmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (pstmt != null) pstmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            DBUtil.closeConnection(conn);
        }
    }

    public List<CollectionData> searchData(String keyword, int page, int pageSize) {
        List<CollectionData> dataList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            int start = (page - 1) * pageSize;

            String sql = "SELECT * FROM collection_data " +
                         "WHERE sku LIKE ? OR seller LIKE ? OR isc1 LIKE ? " +
                         "ORDER BY id ASC LIMIT ?, ?";
            
            pstmt = conn.prepareStatement(sql);
            String likeKeyword = "%" + keyword + "%";
            pstmt.setString(1, likeKeyword);
            pstmt.setString(2, likeKeyword);
            pstmt.setString(3, likeKeyword);
            pstmt.setInt(4, start);
            pstmt.setInt(5, pageSize);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                CollectionData data = new CollectionData();
                data.setId(rs.getInt("id"));
                data.setSku(rs.getString("sku"));
                data.setSeller(rs.getString("seller"));
                data.setIsc1(rs.getString("isc1"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setUserOrganization(rs.getString("user_organization"));
                dataList.add(data);
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
        return dataList;
    }

    public boolean updateData(CollectionData data) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE collection_data SET " +
                         "sku = ?, " +
                         "seller = ?, " +
                         "isc1 = ?, " +
                         "update_user_id = ?, " +
                         "user_id_ding = ? " +
                         "WHERE id = ?";
            
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, data.getSku());
            pstmt.setString(2, data.getSeller());
            pstmt.setString(3, data.getIsc1());
            pstmt.setInt(4, data.getUpdateUserId());
            pstmt.setString(5, data.getUserIdDing());
            pstmt.setInt(6, data.getId());
            
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    public boolean isSkuExists(String sku) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM collection_data WHERE sku = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sku);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                int count = rs.getInt(1);
                return count > 0;
            }
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    public CollectionData getDataById(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT * FROM collection_data WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                CollectionData data = new CollectionData();
                data.setId(rs.getInt("id"));
                data.setSku(rs.getString("sku"));
                data.setSeller(rs.getString("seller"));
                data.setIsc1(rs.getString("isc1"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setUserOrganization(rs.getString("user_organization"));
                return data;
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
        return null;
    }

    public List<CollectionData> getAllData(int page, int pageSize) {
        List<CollectionData> dataList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            int start = (page - 1) * pageSize;
            String sql = "SELECT * FROM collection_data ORDER BY id ASC LIMIT ?, ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, start);
            pstmt.setInt(2, pageSize);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                CollectionData data = new CollectionData();
                data.setId(rs.getInt("id"));
                data.setSku(rs.getString("sku"));
                data.setSeller(rs.getString("seller"));
                data.setIsc1(rs.getString("isc1"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setUserOrganization(rs.getString("user_organization"));
                dataList.add(data);
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
        return dataList;
    }

    public List<CollectionData> getEmptySellerData(String keyword, int page, int pageSize) {
        List<CollectionData> dataList = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            int start = (page - 1) * pageSize;

            String sql = "SELECT * FROM collection_data WHERE (seller IS NULL OR seller = '') ";

            if (keyword != null && !keyword.trim().isEmpty()) {
                sql += "AND (sku LIKE ? OR isc1 LIKE ?)";
            }

            sql += " ORDER BY id ASC LIMIT ?, ?";

            pstmt = conn.prepareStatement(sql);
            int paramIndex = 1;

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                pstmt.setString(paramIndex++, likeKeyword);
                pstmt.setString(paramIndex++, likeKeyword);
            }

            pstmt.setInt(paramIndex++, start);
            pstmt.setInt(paramIndex, pageSize);

            rs = pstmt.executeQuery();

            while (rs.next()) {
                CollectionData data = new CollectionData();
                data.setId(rs.getInt("id"));
                data.setSku(rs.getString("sku"));
                data.setSeller(rs.getString("seller"));
                data.setIsc1(rs.getString("isc1"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setUserOrganization(rs.getString("user_organization"));
                dataList.add(data);
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
        return dataList;
    }

    public int getTotalEmptySellerCount(String keyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DBUtil.getConnection();

            String sql = "SELECT COUNT(*) FROM collection_data WHERE (seller IS NULL OR seller = '') ";

            if (keyword != null && !keyword.trim().isEmpty()) {
                sql += "AND (sku LIKE ? OR isc1 LIKE ?)";
            }

            pstmt = conn.prepareStatement(sql);

            if (keyword != null && !keyword.trim().isEmpty()) {
                String likeKeyword = "%" + keyword + "%";
                pstmt.setString(1, likeKeyword);
                pstmt.setString(2, likeKeyword);
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
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
        return count;
    }

    public int getTotalCount() {
        return getTotalCountWithKeyword(null);
    }

    public List<CollectionData> searchAllData(String keyword) {
        List<CollectionData> dataList = new ArrayList<>();
        String sql = "SELECT id, sku, seller, isc1, sales_depart, user_organization " +
                     "FROM collection_data " +
                     "WHERE sku LIKE ? OR seller LIKE ? OR isc1 LIKE ? " +
                     "ORDER BY id DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchKeyword = "%" + (keyword != null ? keyword : "") + "%";
            pstmt.setString(1, searchKeyword);
            pstmt.setString(2, searchKeyword);
            pstmt.setString(3, searchKeyword);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CollectionData data = new CollectionData();
                data.setId(rs.getInt("id"));
                data.setSku(rs.getString("sku"));
                data.setSeller(rs.getString("seller"));
                data.setIsc1(rs.getString("isc1"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setUserOrganization(rs.getString("user_organization"));
                dataList.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public List<CollectionData> getEmptySellerDataAll(String keyword) {
        List<CollectionData> dataList = new ArrayList<>();
        String sql = "SELECT id, sku, seller, isc1, sales_depart, user_organization " +
                     "FROM collection_data " +
                     "WHERE (seller IS NULL OR seller = '') " +
                     "AND (sku LIKE ? OR isc1 LIKE ?) " +
                     "ORDER BY id DESC";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String searchKeyword = "%" + (keyword != null ? keyword : "") + "%";
            pstmt.setString(1, searchKeyword);
            pstmt.setString(2, searchKeyword);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                CollectionData data = new CollectionData();
                data.setId(rs.getInt("id"));
                data.setSku(rs.getString("sku"));
                data.setSeller(rs.getString("seller"));
                data.setIsc1(rs.getString("isc1"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setUserOrganization(rs.getString("user_organization"));
                dataList.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dataList;
    }

    public int getTotalCountWithKeyword(String keyword) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DBUtil.getConnection();

            String sql;
            if (keyword == null || keyword.trim().isEmpty()) {
                sql = "SELECT COUNT(*) FROM collection_data";
                pstmt = conn.prepareStatement(sql);
            } else {
                sql = "SELECT COUNT(*) FROM collection_data " +
                      "WHERE sku LIKE ? OR seller LIKE ? OR isc1 LIKE ?";
                pstmt = conn.prepareStatement(sql);
                String likeKeyword = "%" + keyword + "%";
                pstmt.setString(1, likeKeyword);
                pstmt.setString(2, likeKeyword);
                pstmt.setString(3, likeKeyword);
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
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
        return count;
    }

    // ========== 新表 collection_new 操作（已修正） ==========

    // 查询新表分页数据 —— ✅ 已修正字段映射
 // 查询新表分页数据 —— ✅ 已修正字段映射（含 sales_depart, uac_id）
 // 查询新表分页数据 —— ✅ 已修正字段映射（含 sales_depart, uac_id, create_user_id）
    public List<CollectionData> getAllNewData(int page, int pageSize) {
        List<CollectionData> list = new ArrayList<>();
        // 👇 添加 id 字段到 SELECT
        String sql = "SELECT id, account, `Campaign name`, Amount, Currency, account_id, warehouse_sku, spu, sales_depart, uac_id, create_user_id FROM tiktok_snull LIMIT ?, ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, (page - 1) * pageSize);
            stmt.setInt(2, pageSize);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CollectionData data = new CollectionData();
                data.setId(rs.getInt("id")); // 👈 设置 id（关键修复！）
                data.setAccount(rs.getString("account"));
                data.setCampaignName(rs.getString("Campaign name"));
                data.setAmount(rs.getString("Amount"));
                data.setCurrency(rs.getString("Currency"));
                data.setAccountId(rs.getString("account_id"));
                data.setWarehouseSku(rs.getString("warehouse_sku"));
                data.setSpu(rs.getString("spu"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setUacId(rs.getString("uac_id"));
                data.setCreateUserId(rs.getInt("create_user_id"));
                list.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }



    // 查询新表总记录数 —— ✅ 无需改字段，COUNT(*) 即可
    public int getTotalNewCount() {
        String sql = "SELECT COUNT(*)  FROM tiktok_snull";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
 // 根据ID查询新表单条数据
    public CollectionData getDataByIdNew(int id) {
        String sql = "SELECT id, account, `Campaign name`, warehouse_sku, create_user_id FROM tiktok_snull WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                CollectionData data = new CollectionData();
                data.setId(rs.getInt("id"));
                data.setAccount(rs.getString("account"));
                data.setCampaignName(rs.getString("Campaign name"));
                data.setWarehouseSku(rs.getString("warehouse_sku"));
                data.setCreateUserId(rs.getInt("create_user_id"));
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
 // 更新新表数据（只更新 warehouse_sku 和 update_user_id）
    public boolean updateNewData(CollectionData data) {
        String sql = "UPDATE tiktok_snull SET warehouse_sku = ?, update_user_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, data.getWarehouseSku());
            stmt.setInt(2, data.getUpdateUserId());
            stmt.setInt(3, data.getId());
            
            int rows = stmt.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean existsWarehouseSku(String sku) {
        // 确保输入不为空
        if (sku == null || sku.trim().isEmpty()) {
            return false;
        }
        
        String sql = "SELECT 1 FROM warehouse_sku WHERE warehouse_sku = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, sku);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next(); // 只需检查是否存在，不需获取实际数据
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 搜索新表数据（支持模糊搜索和筛选）
     * @param keyword 搜索关键词（可为空）
     * @param needEdit 是否只显示需要编辑的数据（create_user_id=1）
     * @param page 页码
     * @param pageSize 每页条数
     * @return 符合条件的数据列表
     */
    public List<CollectionData> searchNewDataWithFilter(String keyword, boolean needEdit, int page, int pageSize) {
        List<CollectionData> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT id, account, `Campaign name`, Amount, Currency, account_id, warehouse_sku, spu, sales_depart, uac_id, create_user_id FROM tiktok_snull");
        
        // 构建 WHERE 条件
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        
        // 👇 新增：排除 Amount 为 NULL 或 0 的记录
        conditions.add("(Amount IS NOT NULL AND Amount != 0)");
        
        // 添加搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword + "%";
            conditions.add("(account LIKE ? OR `Campaign name` LIKE ? OR Amount LIKE ?)");
            parameters.add(likeKeyword);
            parameters.add(likeKeyword);
            parameters.add(likeKeyword);
        }
        
        // 添加筛选条件
        if (needEdit) {
            conditions.add("create_user_id = 1");
        }
        
        // 组合 WHERE 条件
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        
        // 添加分页（注意：你原先是 ASC，保持一致）
        sql.append(" ORDER BY id ASC LIMIT ?, ?");
        parameters.add((page - 1) * pageSize);
        parameters.add(pageSize);
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // 设置所有参数
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                }
                // 可以添加更多类型处理
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                CollectionData data = new CollectionData();
                data.setId(rs.getInt("id"));
                data.setAccount(rs.getString("account"));
                data.setCampaignName(rs.getString("Campaign name"));
                data.setAmount(rs.getString("Amount"));
                data.setCurrency(rs.getString("Currency"));
                data.setAccountId(rs.getString("account_id"));
                data.setWarehouseSku(rs.getString("warehouse_sku"));
                data.setSpu(rs.getString("spu"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setUacId(rs.getString("uac_id"));
                data.setCreateUserId(rs.getInt("create_user_id"));
                list.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 查询新表总记录数（支持搜索和筛选）
     * @param keyword 搜索关键词（可为空）
     * @param needEdit 是否只显示需要编辑的数据（create_user_id=1）
     * @return 符合条件的总记录数
     */
    public int getTotalNewCountWithFilter(String keyword, boolean needEdit) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM tiktok_snull");
        
        // 构建 WHERE 条件
        List<String> conditions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();
        
        // 👇 新增：排除 Amount 为 NULL 或 0 的记录
        conditions.add("(Amount IS NOT NULL AND Amount != 0)");
        
        // 添加搜索条件
        if (keyword != null && !keyword.trim().isEmpty()) {
            String likeKeyword = "%" + keyword + "%";
            conditions.add("(account LIKE ? OR `Campaign name` LIKE ? OR Amount LIKE ?)");
            parameters.add(likeKeyword);
            parameters.add(likeKeyword);
            parameters.add(likeKeyword);
        }
        
        // 添加筛选条件
        if (needEdit) {
            conditions.add("create_user_id = 1");
        }
        
        // 组合 WHERE 条件
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            
            // 设置所有参数
            for (int i = 0; i < parameters.size(); i++) {
                Object param = parameters.get(i);
                if (param instanceof String) {
                    stmt.setString(i + 1, (String) param);
                } else if (param instanceof Integer) {
                    stmt.setInt(i + 1, (Integer) param);
                }
                // 可以添加更多类型处理
            }
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

}
