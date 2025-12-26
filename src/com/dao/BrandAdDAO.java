package com.dao;

import com.model.BrandAd;
import com.util.DBUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BrandAdDAO {

    // ✅ 重构部门条件构建：包含公共数据(sales_depart=0)
    private String buildDeptCondition(String userDepart, List<String> paramList) {
        // 超级管理员(00)和特殊权限(99)：无限制
        if ("00".equals(userDepart) || "99".equals(userDepart)) {
            return "";
        }
        
        // 无部门权限：只看公共数据
        if (userDepart == null || userDepart.trim().isEmpty()) {
            return " AND sales_depart = 0 ";
        }

        // 普通用户：自己的部门 + 公共数据
        String[] depts = userDepart.split(",");
        StringBuilder condition = new StringBuilder(" AND ( sales_depart IN (");
        
        for (int i = 0; i < depts.length; i++) {
            if (i > 0) condition.append(",");
            condition.append("?");
            paramList.add(depts[i].trim());
        }
        condition.append(") OR sales_depart = 0 )");
        return condition.toString();
    }

    // ✅ 默认查询（带分页）：包含公共数据
    public List<BrandAd> findWhereSkuIsEmptyWithPagination(String userDepart, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE (sku IS NULL OR sku = '')");
        List<String> params = new ArrayList<>();
        where.append(buildDeptCondition(userDepart, params)); // 使用新权限逻辑
        return searchByConditionWithPagination(where.toString(), params, page, size);
    }

    // ✅ 统计默认查询：包含公共数据
    public int countWhereSkuIsEmpty(String userDepart) {
        StringBuilder where = new StringBuilder(" WHERE (sku IS NULL OR sku = '')");
        List<String> params = new ArrayList<>();
        where.append(buildDeptCondition(userDepart, params)); // 使用新权限逻辑
        return countByCondition(where.toString(), params);
    }

    // ✅ 模糊搜索（带分页）：包含公共数据
    public List<BrandAd> searchWithPagination(String userDepart, String campaignName, String sku, String uacs, int page, int size) {
        StringBuilder where = new StringBuilder(" WHERE (sku IS NULL OR sku = '')");
        List<String> params = new ArrayList<>();
        where.append(buildDeptCondition(userDepart, params)); // 统一使用新权限逻辑

        // 搜索条件
        if (campaignName != null && !campaignName.trim().isEmpty()) {
            where.append(" AND campaign_name LIKE ? ");
            params.add("%" + campaignName.trim() + "%");
        }
        if (sku != null && !sku.trim().isEmpty()) {
            where.append(" AND sku LIKE ? ");
            params.add("%" + sku.trim() + "%");
        }
        if (uacs != null && !uacs.trim().isEmpty()) {
            where.append(" AND uacs LIKE ? ");
            params.add("%" + uacs.trim() + "%");
        }

        return searchByConditionWithPagination(where.toString(), params, page, size);
    }

    // ✅ 统计模糊搜索：包含公共数据
    public int countSearch(String userDepart, String campaignName, String sku, String uacs) {
        StringBuilder where = new StringBuilder(" WHERE (sku IS NULL OR sku = '')");
        List<String> params = new ArrayList<>();
        where.append(buildDeptCondition(userDepart, params)); // 统一使用新权限逻辑

        if (campaignName != null && !campaignName.trim().isEmpty()) {
            where.append(" AND campaign_name LIKE ? ");
            params.add("%" + campaignName.trim() + "%");
        }
        if (sku != null && !sku.trim().isEmpty()) {
            where.append(" AND sku LIKE ? ");
            params.add("%" + sku.trim() + "%");
        }
        if (uacs != null && !uacs.trim().isEmpty()) {
            where.append(" AND uacs LIKE ? ");
            params.add("%" + uacs.trim() + "%");
        }

        return countByCondition(where.toString(), params);
    }

    // 根据 ID 更新 SKU
    public boolean updateSkuById(int id, String sku) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE brand_ad_data SET sku = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sku != null ? sku.trim() : "");
            pstmt.setInt(2, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            closeResources(null, pstmt, conn);
        }
    }

    private Integer getIntegerFromResultSet(ResultSet rs, String column) throws SQLException {
        Object obj = rs.getObject(column);
        if (obj == null) {
            return null;
        }
        if (obj instanceof Number) {
            return ((Number) obj).intValue();
        }
        try {
            return Integer.parseInt(obj.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // 根据 ID 查询 BrandAd 对象
    public BrandAd findById(int id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, campaign_name, sku, uacs, uac_id, sales_depart, warehouse_sku, quantity, " +
                      "seller_name, " +
                    "sbsku1, sbquantity1, sbsku2, sbquantity2, sbsku3, sbquantity3, " +
                    "sbsku4, sbquantity4, sbsku5, sbquantity5 " +
                    "FROM brand_ad_data WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            
            rs = pstmt.executeQuery();
            if (rs.next()) {
                BrandAd ad = new BrandAd();
                ad.setId(rs.getInt("id"));
                ad.setCampaignName(rs.getString("campaign_name"));
                ad.setSku(rs.getString("sku"));
                ad.setUacs(rs.getString("uacs"));
                ad.setSalesDepart(rs.getString("sales_depart"));
                ad.setWarehouseSku(rs.getString("warehouse_sku"));
                ad.setQuantity(rs.getInt("quantity"));
                ad.setUacId(rs.getString("uac_id")); 
                ad.setSbsku1(rs.getString("sbsku1"));
                ad.setSbquantity1(getIntegerFromResultSet(rs, "sbquantity1"));
                ad.setSbsku2(rs.getString("sbsku2"));
                ad.setSbquantity2(getIntegerFromResultSet(rs, "sbquantity2"));
                ad.setSbsku3(rs.getString("sbsku3"));
                ad.setSbquantity3(getIntegerFromResultSet(rs, "sbquantity3"));
                ad.setSbsku4(rs.getString("sbsku4"));
                ad.setSbquantity4(getIntegerFromResultSet(rs, "sbquantity4"));
                ad.setSbsku5(rs.getString("sbsku5"));
                ad.setSbquantity5(getIntegerFromResultSet(rs, "sbquantity5"));
                ad.setSellerName(rs.getString("seller_name"));
                
                return ad;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return null;
    }

    // ==================== 私有方法 ====================

    private List<BrandAd> searchByConditionWithPagination(String whereClause, List<String> params, int page, int size) {
        List<BrandAd> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, campaign_name, sku, uacs, uac_id, sales_depart, warehouse_sku, quantity, " +
                      "seller_name, " +
                     "sbsku1, sbquantity1, sbsku2, sbquantity2, sbsku3, sbquantity3, " +
                     "sbsku4, sbquantity4, sbsku5, sbquantity5 " +
                     "FROM brand_ad_data" +
                     whereClause + " ORDER BY id LIMIT ?, ?";
            pstmt = conn.prepareStatement(sql);

            int index = 1;
            for (String param : params) {
                pstmt.setString(index++, param);
            }
            pstmt.setInt(index++, (page - 1) * size);
            pstmt.setInt(index, size);

            rs = pstmt.executeQuery();
            while (rs.next()) {
                BrandAd ad = new BrandAd();
                ad.setId(rs.getInt("id"));          
                ad.setCampaignName(rs.getString("campaign_name"));
                ad.setSku(rs.getString("sku"));
                ad.setUacs(rs.getString("uacs"));               
                ad.setSalesDepart(rs.getString("sales_depart"));
                ad.setWarehouseSku(rs.getString("warehouse_sku"));
                ad.setQuantity(rs.getInt("quantity"));
                ad.setUacId(rs.getString("uac_id"));
                ad.setSbsku1(rs.getString("sbsku1"));
                ad.setSbquantity1(getIntegerFromResultSet(rs, "sbquantity1"));
                ad.setSbsku2(rs.getString("sbsku2"));
                ad.setSbquantity2(getIntegerFromResultSet(rs, "sbquantity2"));
                ad.setSbsku3(rs.getString("sbsku3"));
                ad.setSbquantity3(getIntegerFromResultSet(rs, "sbquantity3"));
                ad.setSbsku4(rs.getString("sbsku4"));
                ad.setSbquantity4(getIntegerFromResultSet(rs, "sbquantity4"));
                ad.setSbsku5(rs.getString("sbsku5"));
                ad.setSbquantity5(getIntegerFromResultSet(rs, "sbquantity5"));
                ad.setSellerName(rs.getString("seller_name"));
                
                list.add(ad);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return list;
    }

    private int countByCondition(String whereClause, List<String> params) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT COUNT(*) FROM brand_ad_data" + whereClause;
            pstmt = conn.prepareStatement(sql);

            int index = 1;
            for (String param : params) {
                pstmt.setString(index++, param);
            }

            rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }
        return 0;
    }

    private void closeResources(ResultSet rs, PreparedStatement pstmt, Connection conn) {
        if (rs != null) try { rs.close(); } catch (Exception e) { /* ignore */ }
        if (pstmt != null) try { pstmt.close(); } catch (Exception e) { /* ignore */ }
        DBUtil.closeConnection(conn);
    }
    
    public boolean updateSkuAndUserId(int id, String sku, int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE brand_ad_data SET sku = ?, update_use_id = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sku != null ? sku.trim() : "");
            pstmt.setInt(2, userId);
            pstmt.setInt(3, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public boolean updateSkuAndWarehouseSkuAndQuantityAndUserId(
            int id, String sku, String warehouseSku, int quantity, int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE brand_ad_data SET sku = ?, warehouse_sku = ?, quantity = ?, update_use_id = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sku != null ? sku.trim() : "");
            pstmt.setString(2, warehouseSku != null ? warehouseSku.trim() : null);
            pstmt.setInt(3, quantity);
            pstmt.setInt(4, userId);
            pstmt.setInt(5, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public boolean updateSbsku1AndSbquantity1(int id, String sbsku1, Integer sbquantity1) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE brand_ad_data SET sbsku1 = ?, sbquantity1 = ? WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sbsku1 != null ? sbsku1.trim() : null);
            pstmt.setObject(2, sbquantity1);
            pstmt.setInt(3, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }
    
    public boolean updateSkuAndWarehouseSkuAndQuantityAndSbsku1AndSbquantity1AndUserId(
            int id, String sku, String warehouseSku, int quantity, String sellerName,
            String sbsku1, Integer sbquantity1,
            String sbsku2, Integer sbquantity2,
            String sbsku3, Integer sbquantity3,
            String sbsku4, Integer sbquantity4,
            String sbsku5, Integer sbquantity5,
            int userId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE brand_ad_data SET " +
                         "sku = ?, warehouse_sku = ?, quantity = ?, " +
                         "seller_name = ?, " +
                         "sbsku1 = ?, sbquantity1 = ?, " +
                         "sbsku2 = ?, sbquantity2 = ?, " +
                         "sbsku3 = ?, sbquantity3 = ?, " +
                         "sbsku4 = ?, sbquantity4 = ?, " +
                         "sbsku5 = ?, sbquantity5 = ?, " +
                         "update_use_id = ? " +
                         "WHERE id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, sku != null ? sku.trim() : "");
            pstmt.setString(2, warehouseSku != null ? warehouseSku.trim() : null);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, sellerName != null ? sellerName.trim() : null);
            pstmt.setString(5, sbsku1 != null ? sbsku1.trim() : null);
            pstmt.setObject(6, sbquantity1);
            pstmt.setString(7, sbsku2 != null ? sbsku2.trim() : null);
            pstmt.setObject(8, sbquantity2);
            pstmt.setString(9, sbsku3 != null ? sbsku3.trim() : null);
            pstmt.setObject(10, sbquantity3);
            pstmt.setString(11, sbsku4 != null ? sbsku4.trim() : null);
            pstmt.setObject(12, sbquantity4);
            pstmt.setString(13, sbsku5 != null ? sbsku5.trim() : null);
            pstmt.setObject(14, sbquantity5);
            pstmt.setInt(15, userId);
            pstmt.setInt(16, id);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBUtil.closeConnection(conn);
        }
    }

    public boolean updateBrandAdMap(int adId, String platformSku, String uacId,
                                    List<Map.Entry<String, Integer>> warehouseItems) {
        Connection conn = null;
        PreparedStatement insertStmt = null;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);
            
            // 清除旧映射
            String deleteSql = "DELETE FROM brand_ad_map WHERE ad_id = ?";
            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
                deleteStmt.setInt(1, adId);
                deleteStmt.executeUpdate();
            }

            // 批量插入新映射
            if (!warehouseItems.isEmpty()) {
                String insertSql = "INSERT INTO brand_ad_map (platform_sku, warehouse_sku, quantity, uac_id, ad_id) VALUES (?, ?, ?, ?, ?)";
                insertStmt = conn.prepareStatement(insertSql);

                for (Map.Entry<String, Integer> item : warehouseItems) {
                    insertStmt.setString(1, platformSku);
                    insertStmt.setString(2, item.getKey());
                    insertStmt.setObject(3, item.getValue());
                    insertStmt.setString(4, uacId);
                    insertStmt.setInt(5, adId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return false;
        } finally {
            try {
                if (insertStmt != null) insertStmt.close();
            } catch (SQLException ignored) {}
            DBUtil.closeConnection(conn);
        }
    }
}
