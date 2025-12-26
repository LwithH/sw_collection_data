package com.dao;

import com.model.AmazonData;
import com.model.BatchAddRequest;
import com.servlet.BatchAddAmazonDataServlet;
import com.util.DBUtil;
import com.google.gson.Gson;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import com.model.BatchAddRequest;
public class AmazonDataDAO {

	public List<AmazonData> getAmazonDataByPage(
	        String searchField, String keyword,
	        String searchField2, String keyword2, // 新增第二个搜索条件
	        boolean filterEmptySeller,
	        Integer year,
	        int offset, int limit) {

	    StringBuilder sql = new StringBuilder(
	        "SELECT id, uacs, sku, seller, sales_depart, title, " +
	        "asin, parent_asin, lifecl, isc1, sku_last_date, update_user_id " +
	        "FROM amazon_data WHERE 1=1"
	    );

	    List<Object> params = new ArrayList<>();

	    // 1. 第一个模糊搜索（防SQL注入）→ ✅ 使用数据库真实字段名
	    if (searchField != null && !searchField.isEmpty() && keyword != null && !keyword.isEmpty()) {
	        List<String> allowedFields = Arrays.asList(
	            "id", "uacs", "sku", "seller", "sales_depart",
	            "title", "asin", "parent_asin", "lifecl", "isc1"
	        );
	        if (allowedFields.contains(searchField)) {
	            // 特殊处理部门字段：支持多值查询（逗号分隔）
	            if ("sales_depart".equals(searchField)) {
	                if (keyword.contains(",")) {
	                    // 多个部门代码，使用IN查询
	                    String[] deptCodes = keyword.split(",");
	                    StringBuilder inClause = new StringBuilder();
	                    for (int i = 0; i < deptCodes.length; i++) {
	                        if (i > 0) inClause.append(",");
	                        inClause.append("?");
	                        params.add(deptCodes[i].trim());
	                    }
	                    sql.append(" AND ").append(searchField).append(" IN (").append(inClause).append(")");
	                } else {
	                    // 单个部门代码，使用等于查询
	                    sql.append(" AND ").append(searchField).append(" = ?");
	                    params.add(keyword.trim());
	                }
	            } else {
	                // 其他字段正常LIKE查询
	                sql.append(" AND ").append(searchField).append(" LIKE ?");
	                params.add("%" + keyword + "%");
	            }
	        }
	    }

	    // 1.1 第二个模糊搜索（防SQL注入）
	    if (searchField2 != null && !searchField2.isEmpty() && keyword2 != null && !keyword2.isEmpty()) {
	        List<String> allowedFields = Arrays.asList(
	            "id", "uacs", "sku", "seller", "sales_depart",
	            "title", "asin", "parent_asin", "lifecl", "isc1"
	        );
	        if (allowedFields.contains(searchField2)) {
	            // 特殊处理部门字段：支持多值查询（逗号分隔）
	            if ("sales_depart".equals(searchField2)) {
	                if (keyword2.contains(",")) {
	                    // 多个部门代码，使用IN查询
	                    String[] deptCodes = keyword2.split(",");
	                    StringBuilder inClause = new StringBuilder();
	                    for (int i = 0; i < deptCodes.length; i++) {
	                        if (i > 0) inClause.append(",");
	                        inClause.append("?");
	                        params.add(deptCodes[i].trim());
	                    }
	                    sql.append(" AND ").append(searchField2).append(" IN (").append(inClause).append(")");
	                } else {
	                    // 单个部门代码，使用等于查询
	                    sql.append(" AND ").append(searchField2).append(" = ?");
	                    params.add(keyword2.trim());
	                }
	            } else {
	                // 其他字段正常LIKE查询
	                sql.append(" AND ").append(searchField2).append(" LIKE ?");
	                params.add("%" + keyword2 + "%");
	            }
	        }
	    }

	    // 2. Seller 为空筛选
	    if (filterEmptySeller) {
	        sql.append(" AND (seller IS NULL OR seller = '')");
	    }

	    // 3. 年份筛选
	    if (year != null) {
	        LocalDate start = LocalDate.of(year, 1, 1);
	        LocalDate end = LocalDate.of(year + 1, 1, 1);
	        sql.append(" AND sku_last_date >= ? AND sku_last_date < ?");
	        params.add(Date.valueOf(start));
	        params.add(Date.valueOf(end));
	    }

	    sql.append(" ORDER BY id, uacs LIMIT ? OFFSET ?");
	    params.add(limit);
	    params.add(offset);

	    return executeQuery(sql.toString(), params);
	}

	public int getTotalCount(String searchField, String keyword, 
	                         String searchField2, String keyword2, // 新增第二个搜索条件
	                         boolean filterEmptySeller, Integer year) {
	    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM amazon_data WHERE 1=1");
	    List<Object> params = new ArrayList<>();

	    // 1. 第一个模糊搜索
	    if (searchField != null && !searchField.isEmpty() && keyword != null && !keyword.isEmpty()) {
	        List<String> allowedFields = Arrays.asList(
	            "id", "uacs", "sku", "seller", "sales_depart",
	            "title", "asin", "parent_asin", "lifecl", "isc1"
	        );
	        if (allowedFields.contains(searchField)) {
	            // 特殊处理部门字段：支持多值查询（逗号分隔）
	            if ("sales_depart".equals(searchField)) {
	                if (keyword.contains(",")) {
	                    // 多个部门代码，使用IN查询
	                    String[] deptCodes = keyword.split(",");
	                    StringBuilder inClause = new StringBuilder();
	                    for (int i = 0; i < deptCodes.length; i++) {
	                        if (i > 0) inClause.append(",");
	                        inClause.append("?");
	                        params.add(deptCodes[i].trim());
	                    }
	                    sql.append(" AND ").append(searchField).append(" IN (").append(inClause).append(")");
	                } else {
	                    // 单个部门代码，使用等于查询
	                    sql.append(" AND ").append(searchField).append(" = ?");
	                    params.add(keyword.trim());
	                }
	            } else {
	                // 其他字段正常LIKE查询
	                sql.append(" AND ").append(searchField).append(" LIKE ?");
	                params.add("%" + keyword + "%");
	            }
	        }
	    }

	    // 1.1 第二个模糊搜索
	    if (searchField2 != null && !searchField2.isEmpty() && keyword2 != null && !keyword2.isEmpty()) {
	        List<String> allowedFields = Arrays.asList(
	            "id", "uacs", "sku", "seller", "sales_depart",
	            "title", "asin", "parent_asin", "lifecl", "isc1"
	        );
	        if (allowedFields.contains(searchField2)) {
	            // 特殊处理部门字段：支持多值查询（逗号分隔）
	            if ("sales_depart".equals(searchField2)) {
	                if (keyword2.contains(",")) {
	                    // 多个部门代码，使用IN查询
	                    String[] deptCodes = keyword2.split(",");
	                    StringBuilder inClause = new StringBuilder();
	                    for (int i = 0; i < deptCodes.length; i++) {
	                        if (i > 0) inClause.append(",");
	                        inClause.append("?");
	                        params.add(deptCodes[i].trim());
	                    }
	                    sql.append(" AND ").append(searchField2).append(" IN (").append(inClause).append(")");
	                } else {
	                    // 单个部门代码，使用等于查询
	                    sql.append(" AND ").append(searchField2).append(" = ?");
	                    params.add(keyword2.trim());
	                }
	            } else {
	                // 其他字段正常LIKE查询
	                sql.append(" AND ").append(searchField2).append(" LIKE ?");
	                params.add("%" + keyword2 + "%");
	            }
	        }
	    }

	    if (filterEmptySeller) {
	        sql.append(" AND (seller IS NULL OR seller = '')");
	    }

	    if (year != null) {
	        sql.append(" AND YEAR(sku_last_date) = ?");
	        params.add(year);
	    }

	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
	        for (int i = 0; i < params.size(); i++) {
	            stmt.setObject(i + 1, params.get(i));
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

	public int getEmptySellerCount(String searchField, String keyword, 
	                              String searchField2, String keyword2, // 新增第二个搜索条件
	                              Integer year) {
	    StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM amazon_data WHERE (seller IS NULL OR seller = '')");
	    List<Object> params = new ArrayList<>();

	    // 添加第一个搜索条件
	    if (searchField != null && !searchField.isEmpty() && keyword != null && !keyword.isEmpty()) {
	        List<String> allowedFields = Arrays.asList(
	            "id", "uacs", "sku", "seller", "sales_depart",
	            "title", "asin", "parent_asin", "lifecl", "isc1"
	        );
	        if (allowedFields.contains(searchField)) {
	            // 特殊处理部门字段：支持多值查询（逗号分隔）
	            if ("sales_depart".equals(searchField)) {
	                if (keyword.contains(",")) {
	                    // 多个部门代码，使用IN查询
	                    String[] deptCodes = keyword.split(",");
	                    StringBuilder inClause = new StringBuilder();
	                    for (int i = 0; i < deptCodes.length; i++) {
	                        if (i > 0) inClause.append(",");
	                        inClause.append("?");
	                        params.add(deptCodes[i].trim());
	                    }
	                    sql.append(" AND ").append(searchField).append(" IN (").append(inClause).append(")");
	                } else {
	                    // 单个部门代码，使用等于查询
	                    sql.append(" AND ").append(searchField).append(" = ?");
	                    params.add(keyword.trim());
	                }
	            } else {
	                // 其他字段正常LIKE查询
	                sql.append(" AND ").append(searchField).append(" LIKE ?");
	                params.add("%" + keyword + "%");
	            }
	        }
	    }

	    // 添加第二个搜索条件
	    if (searchField2 != null && !searchField2.isEmpty() && keyword2 != null && !keyword2.isEmpty()) {
	        List<String> allowedFields = Arrays.asList(
	            "id", "uacs", "sku", "seller", "sales_depart",
	            "title", "asin", "parent_asin", "lifecl", "isc1"
	        );
	        if (allowedFields.contains(searchField2)) {
	            // 特殊处理部门字段：支持多值查询（逗号分隔）
	            if ("sales_depart".equals(searchField2)) {
	                if (keyword2.contains(",")) {
	                    // 多个部门代码，使用IN查询
	                    String[] deptCodes = keyword2.split(",");
	                    StringBuilder inClause = new StringBuilder();
	                    for (int i = 0; i < deptCodes.length; i++) {
	                        if (i > 0) inClause.append(",");
	                        inClause.append("?");
	                        params.add(deptCodes[i].trim());
	                    }
	                    sql.append(" AND ").append(searchField2).append(" IN (").append(inClause).append(")");
	                } else {
	                    // 单个部门代码，使用等于查询
	                    sql.append(" AND ").append(searchField2).append(" = ?");
	                    params.add(keyword2.trim());
	                }
	            } else {
	                // 其他字段正常LIKE查询
	                sql.append(" AND ").append(searchField2).append(" LIKE ?");
	                params.add("%" + keyword2 + "%");
	            }
	        }
	    }

	    // 添加年份筛选
	    if (year != null) {
	        LocalDate start = LocalDate.of(year, 1, 1);
	        LocalDate end = LocalDate.of(year + 1, 1, 1);
	        sql.append(" AND sku_last_date >= ? AND sku_last_date < ?");
	        params.add(Date.valueOf(start));
	        params.add(Date.valueOf(end));
	    }

	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
	        for (int i = 0; i < params.size(); i++) {
	            stmt.setObject(i + 1, params.get(i));
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

	public int getIdAtOffset(String searchField, String keyword,
	                        String searchField2, String keyword2, // 新增第二个搜索条件
	                        boolean filterEmptySeller, Integer year, int offset) {
	    StringBuilder sql = new StringBuilder("SELECT id FROM amazon_data WHERE 1=1");
	    List<Object> params = new ArrayList<>();

	    // === 复用与 getAmazonDataByPage 相同的 WHERE 条件 ===
	    // 第一个搜索条件
	    if (searchField != null && !searchField.isEmpty() && keyword != null && !keyword.isEmpty()) {
	        List<String> allowedFields = Arrays.asList(
	            "id", "uacs", "sku", "seller", "sales_depart",
	            "title", "asin", "parent_asin", "lifecl", "isc1"
	        );
	        if (allowedFields.contains(searchField)) {
	            // 特殊处理部门字段：支持多值查询（逗号分隔）
	            if ("sales_depart".equals(searchField)) {
	                if (keyword.contains(",")) {
	                    // 多个部门代码，使用IN查询
	                    String[] deptCodes = keyword.split(",");
	                    StringBuilder inClause = new StringBuilder();
	                    for (int i = 0; i < deptCodes.length; i++) {
	                        if (i > 0) inClause.append(",");
	                        inClause.append("?");
	                        params.add(deptCodes[i].trim());
	                    }
	                    sql.append(" AND ").append(searchField).append(" IN (").append(inClause).append(")");
	                } else {
	                    // 单个部门代码，使用等于查询
	                    sql.append(" AND ").append(searchField).append(" = ?");
	                    params.add(keyword.trim());
	                }
	            } else {
	                // 其他字段正常LIKE查询
	                sql.append(" AND ").append(searchField).append(" LIKE ?");
	                params.add("%" + keyword + "%");
	            }
	        }
	    }

	    // 第二个搜索条件
	    if (searchField2 != null && !searchField2.isEmpty() && keyword2 != null && !keyword2.isEmpty()) {
	        List<String> allowedFields = Arrays.asList(
	            "id", "uacs", "sku", "seller", "sales_depart",
	            "title", "asin", "parent_asin", "lifecl", "isc1"
	        );
	        if (allowedFields.contains(searchField2)) {
	            // 特殊处理部门字段：支持多值查询（逗号分隔）
	            if ("sales_depart".equals(searchField2)) {
	                if (keyword2.contains(",")) {
	                    // 多个部门代码，使用IN查询
	                    String[] deptCodes = keyword2.split(",");
	                    StringBuilder inClause = new StringBuilder();
	                    for (int i = 0; i < deptCodes.length; i++) {
	                        if (i > 0) inClause.append(",");
	                        inClause.append("?");
	                        params.add(deptCodes[i].trim());
	                    }
	                    sql.append(" AND ").append(searchField2).append(" IN (").append(inClause).append(")");
	                } else {
	                    // 单个部门代码，使用等于查询
	                    sql.append(" AND ").append(searchField2).append(" = ?");
	                    params.add(keyword2.trim());
	                }
	            } else {
	                // 其他字段正常LIKE查询
	                sql.append(" AND ").append(searchField2).append(" LIKE ?");
	                params.add("%" + keyword2 + "%");
	            }
	        }
	    }

	    if (filterEmptySeller) {
	        sql.append(" AND (seller IS NULL OR seller = '')");
	    }

	    if (year != null) {
	        // ⚠️ 必须与 getTotalCount 等方法保持一致：使用范围查询
	        LocalDate start = LocalDate.of(year, 1, 1);
	        LocalDate end = LocalDate.of(year + 1, 1, 1);
	        sql.append(" AND sku_last_date >= ? AND sku_last_date < ?");
	        params.add(Date.valueOf(start));
	        params.add(Date.valueOf(end));
	    }

	    sql.append(" ORDER BY id LIMIT 1 OFFSET ?");
	    params.add(offset);

	    try (Connection conn = DBUtil.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
	        for (int i = 0; i < params.size(); i++) {
	            stmt.setObject(i + 1, params.get(i));
	        }
	        ResultSet rs = stmt.executeQuery();
	        if (rs.next()) {
	            return rs.getInt("id");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return -1;
	}
    // 抽取公共查询方法（避免重复代码）
    private List<AmazonData> executeQuery(String sql, List<Object> params) {
        List<AmazonData> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AmazonData data = new AmazonData();
                data.setId(rs.getInt("id"));
                data.setUacs(rs.getString("uacs"));
                data.setSku(rs.getString("sku"));
                data.setSeller(rs.getString("seller"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setTitle(rs.getString("title"));
                data.setAsin(rs.getString("asin"));
                data.setParentAsin(rs.getString("parent_asin"));
                data.setLifecl(rs.getString("lifecl"));
                data.setIsc1(rs.getString("isc1"));
                data.setSkuLastDate(rs.getDate("sku_last_date"));
                list.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 更新 Seller、Lifecl 和 Isc1，并记录更新人ID
     */
    public boolean updateSellerLifeclAndIsc1(int id, String seller, String lifecl, String isc1, int updateUserId) {
        String sql = "UPDATE amazon_data SET seller = ?, lifecl = ?, isc1 = ?, update_user_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, seller);
            stmt.setString(2, lifecl);
            stmt.setString(3, isc1);
            stmt.setInt(4, updateUserId);
            stmt.setInt(5, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 保留原有的方法（兼容性）
    public boolean updateSellerAndLifecl(int id, String seller, String lifecl) {
        String sql = "UPDATE amazon_data SET seller = ?, lifecl = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, seller);
            stmt.setString(2, lifecl);
            stmt.setInt(3, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getSellerById(int id) {
        String sql = "SELECT seller FROM amazon_data WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("seller");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 更新 Seller 和 Lifecl，并记录更新人ID
     */
    public boolean updateSellerAndLifecl(int id, String seller, String lifecl, int updateUserId) {
        String sql = "UPDATE amazon_data SET seller = ?, lifecl = ?, update_user_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, seller);
            stmt.setString(2, lifecl);
            stmt.setInt(3, updateUserId); // 新增：记录更新人ID
            stmt.setInt(4, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 新增方法：更新 Seller、Lifecl、Isc1 和 User_id_ding
    public boolean updateSellerLifeclIsc1AndUserIdDing(int id, String seller, String lifecl, String isc1, String userIdDing, int updateUserId) {
        String sql = "UPDATE amazon_data SET seller = ?, lifecl = ?, isc1 = ?, user_id_ding = ?, update_user_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {        
            stmt.setString(1, seller);
            stmt.setString(2, lifecl);
            stmt.setString(3, isc1);
            stmt.setString(4, userIdDing);
            stmt.setInt(5, updateUserId);
            stmt.setInt(6, id);
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("影响行数: " + rowsAffected);
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.out.println("数据库更新异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 选择性更新：只更新非 null 字段
     */
    public boolean updateSelective(int id, String seller, String lifecl, String isc1, String userIdDing, int updateUserId) {
        StringBuilder sql = new StringBuilder("UPDATE amazon_data SET ");
        List<Object> params = new ArrayList<>();

        if (seller != null) {
            sql.append("seller = ?, ");
            params.add(seller);
        }
        if (lifecl != null) {
            sql.append("lifecl = ?, ");
            params.add(lifecl);
        }
        if (isc1 != null) {
            sql.append("isc1 = ?, ");
            params.add(isc1);
        }
        if (userIdDing != null) {
            sql.append("user_id_ding = ?, ");
            params.add(userIdDing);
        }
        // update_user_id 总是更新
        sql.append("update_user_id = ?, ");
        params.add(updateUserId);

        // 移除最后的 ", "
        sql.setLength(sql.length() - 2);
        sql.append(" WHERE id = ?");
        params.add(id);

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                stmt.setObject(i + 1, params.get(i));
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public List<AmazonData> getOriginalDataBatch(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        String placeholders = ids.stream().map(i -> "?").collect(Collectors.joining(","));
        // ✅ 添加 user_id_ding 到 SELECT
        String sql = "SELECT id, uacs, sku, seller, sales_depart, title, " +
                     "asin, parent_asin, lifecl, isc1, sku_last_date, update_user_id, user_id_ding " +
                     "FROM amazon_data WHERE id IN (" + placeholders + ")";

        List<AmazonData> list = new ArrayList<>();
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < ids.size(); i++) {
                stmt.setInt(i + 1, ids.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                AmazonData data = new AmazonData();
                data.setId(rs.getInt("id"));
                data.setUacs(rs.getString("uacs"));
                data.setSku(rs.getString("sku"));
                data.setSeller(rs.getString("seller"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setTitle(rs.getString("title"));
                data.setAsin(rs.getString("asin"));
                data.setParentAsin(rs.getString("parent_asin"));
                data.setLifecl(rs.getString("lifecl"));
                data.setIsc1(rs.getString("isc1"));
                data.setSkuLastDate(rs.getDate("sku_last_date"));
                // ✅ 新增
                data.setUser_id_ding(rs.getString("user_id_ding")); // ← 关键！
                list.add(data);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 批量选择性更新（使用 JDBC Batch）
     * 注意：lifecl 若为 null，将被设为 ""（空字符串），避免 NOT NULL 错误
     */
    public boolean updateBatchSelective(List<BatchUpdateItem> items, int updateUserId) {
        if (items == null || items.isEmpty()) return true;

        String sql = "UPDATE amazon_data SET seller = ?, lifecl = ?, isc1 = ?, " +
                "user_id_ding = ?, sales_depart = ?, update_user_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            conn.setAutoCommit(false);

            for (BatchUpdateItem item : items) {
                String lifecl = (item.lifecl == null) ? "" : item.lifecl;
                stmt.setString(1, item.seller);
                stmt.setString(2, lifecl);
                stmt.setString(3, item.isc1);
                stmt.setString(4, item.userIdDing);
                stmt.setString(5, item.salesDepart); // ← 新增
                stmt.setInt(6, updateUserId);
                stmt.setInt(7, item.id);
                stmt.addBatch();
            }

            stmt.executeBatch();
            conn.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 内部类：用于批量更新
    public static class BatchUpdateItem {
        public int id;
        public String seller;
        public String lifecl;
        public String isc1;
        public String userIdDing;
        public String salesDepart; // ← 新增字段
        public BatchUpdateItem(int id, String seller, String lifecl, String isc1, 
                String userIdDing, String salesDepart) { // ← 新增参数
this.id = id;
this.seller = seller;
this.lifecl = lifecl;
this.isc1 = isc1;
this.userIdDing = userIdDing;
this.salesDepart = salesDepart;
}
    }
 // 在AmazonDataDAO类中添加以下方法
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
     * 插入新的亚马逊数据记录
     */
    public boolean insertAmazonData(AmazonData data) {
    	
        String sql = "INSERT INTO amazon_data (" +
                     "uacs, sku, seller, sales_depart, title, " +
                     "asin, parent_asin, lifecl, warehouse_sku, " +
                     "isc1, sku_last_date, user_id_ding, market_id, uac_id, " +
                     "create_user_id, update_user_id" +
                     ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, data.getUacs());
            stmt.setString(2, data.getSku());
            stmt.setString(3, data.getSeller());
            stmt.setString(4, data.getSalesDepart()); // 确保正确设置sales_depart
            stmt.setString(5, null); // title 可为空
            stmt.setString(6, data.getAsin());
            stmt.setString(7, data.getParentAsin());
            stmt.setString(8, data.getLifecl());
            stmt.setString(9, data.getWarehouseSku());
            stmt.setString(10, data.getIsc1());
            stmt.setDate(11, data.getSkuLastDate());
            stmt.setString(12, data.getUser_id_ding());
            stmt.setString(13, data.getMarketId());
            stmt.setString(14, data.getUacId());
            stmt.setInt(15, data.getCreateUserId());
            stmt.setInt(16, data.getUpdateUserId());
            
            int rowsAffected = stmt.executeUpdate();
            System.out.println("插入记录，影响行数: " + rowsAffected);
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.out.println("插入记录时发生异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取去重后的账户前缀列表
     */
    public List<String> getDistinctUacsPrefixes() {
        List<String> prefixes = new ArrayList<>();
        String sql = "SELECT DISTINCT SUBSTRING_INDEX(uacs, '_', 1) as prefix FROM g_account ORDER BY prefix";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                prefixes.add(rs.getString("prefix"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return prefixes;
    }

    /**
     * 根据前缀获取对应的所有账户
     */
    public List<String> getUacsByPrefix(String prefix) {
        List<String> uacsList = new ArrayList<>();
        String sql = "SELECT uacs FROM g_account WHERE uacs LIKE ? ORDER BY uacs";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, prefix + "_%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                uacsList.add(rs.getString("uacs"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uacsList;
    }
    /*批量插入亚马逊数据*/
    public int batchInsertAmazonData(BatchAddRequest req, int userId) {
        int successCount = 0;
        
        // 首先获取该前缀对应的所有账户
        List<String> uacsList = getUacsByPrefix(req.getPrefix());
        if (uacsList.isEmpty()) {
            return 0;
        }
        
        String sql = "INSERT INTO amazon_data (uacs, sku, seller, sales_depart, title, " +
                     "asin, parent_asin, lifecl, warehouse_sku, isc1, sku_last_date, " +
                     "user_id_ding, market_id, uac_id, create_user_id, update_user_id) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            conn.setAutoCommit(false);
            
            for (String uacs : uacsList) {
                // 获取每个账户的市场信息
                MarketInfo marketInfo = getMarketInfoByUacs(uacs);
                if (marketInfo == null) {
                    continue; // 跳过无效账户
                }
                
                // 获取sales_depart和user_id_ding
                SellerNameDAO sellerDao = new SellerNameDAO();
                String salesDepart = sellerDao.getSalesDepartBySellerName(req.getSeller());
                if (salesDepart == null) {
                    salesDepart = "0";
                }
                String userIdDing = sellerDao.validateSellerAndGetUserIdDing(req.getSeller());
                if (userIdDing == null) {
                    continue; // 跳过无效seller
                }
                
                stmt.setString(1, uacs);
                stmt.setString(2, req.getSku());
                stmt.setString(3, req.getSeller());
                stmt.setString(4, salesDepart);
                stmt.setString(5, null); // title
                stmt.setString(6, req.getAsin());
                stmt.setString(7, req.getParentAsin());
                stmt.setString(8, req.getLifecl());
                stmt.setString(9, req.getWarehouseSku());
                stmt.setString(10, "首位"); // 默认值
                stmt.setDate(11, Date.valueOf(LocalDate.now().minusDays(1)));
                stmt.setString(12, userIdDing);
                stmt.setString(13, marketInfo.marketId);
                stmt.setString(14, marketInfo.uacId);
                stmt.setInt(15, userId);
                stmt.setInt(16, userId);
                
                stmt.addBatch();
                successCount++;
            }
            
            stmt.executeBatch();
            conn.commit();
            
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        
        return successCount;
    }
    
    public int batchInsertAmazonData(BatchAddRequest req, int userId, String userIdDing) {
        if (userIdDing == null || req.getPrefix() == null || req.getSku() == null) {
            return 0;
        }

        // 获取该前缀下的所有账户
        List<String> uacsList = getUacsByPrefix(req.getPrefix());
        if (uacsList == null || uacsList.isEmpty()) {
            return 0;
        }

        // 获取 sales_depart（基于 userIdDing，确保一致性）
        SellerNameDAO sellerDao = new SellerNameDAO();
        String salesDepart = sellerDao.getSalesDepartByUserIdDing(userIdDing);
        if (salesDepart == null) {
            salesDepart = "0";
        }

        Connection conn = null;
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        int successCount = 0;

        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 准备检查语句
            String checkSql = "SELECT 1 FROM amazon_data WHERE uacs = ? AND sku = ? LIMIT 1";
            checkStmt = conn.prepareStatement(checkSql);

            // 2. 准备插入语句
            String insertSql = "INSERT INTO amazon_data (" +
                    "uacs, sku, seller, sales_depart, title, " +
                    "asin, parent_asin, lifecl, warehouse_sku, isc1, sku_last_date, " +
                    "user_id_ding, market_id, uac_id, create_user_id, update_user_id" +
                    ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            insertStmt = conn.prepareStatement(insertSql);

            for (String uacs : uacsList) {
                // 检查该账户下是否已存在此 SKU
                checkStmt.setString(1, uacs);
                checkStmt.setString(2, req.getSku());
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next()) {
                    // ✅ 已存在，跳过
                    continue;
                }
                rs.close();

                // 获取市场信息
                MarketInfo marketInfo = getMarketInfoByUacs(uacs);
                if (marketInfo == null) {
                    continue; // 跳过无效账户
                }

                // 设置插入参数
                insertStmt.setString(1, uacs);
                insertStmt.setString(2, req.getSku());
                insertStmt.setString(3, req.getSeller());
                insertStmt.setString(4, salesDepart);
                insertStmt.setString(5, null); // title
                insertStmt.setString(6, req.getAsin());
                insertStmt.setString(7, req.getParentAsin());
                insertStmt.setString(8, req.getLifecl());
                insertStmt.setString(9, req.getWarehouseSku());
                insertStmt.setString(10, "首位"); // 默认值
                insertStmt.setDate(11, Date.valueOf(LocalDate.now().minusDays(1)));
                insertStmt.setString(12, userIdDing);
                insertStmt.setString(13, marketInfo.marketId);
                insertStmt.setString(14, marketInfo.uacId);
                insertStmt.setInt(15, userId);
                insertStmt.setInt(16, userId);

                int rows = insertStmt.executeUpdate();
                if (rows > 0) {
                    successCount++;
                }

                insertStmt.clearParameters(); // 清理参数（可选，但安全）
            }

            conn.commit();
            return successCount;

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return 0;
        } finally {
            // 安全关闭资源
            if (checkStmt != null) try { checkStmt.close(); } catch (SQLException ignored) {}
            if (insertStmt != null) try { insertStmt.close(); } catch (SQLException ignored) {}
            if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
        }
    }
    /**
     * 检查指定账户中SKU是否重复
     */
    public boolean isSkuDuplicateInUacs(String sku, String uacs) {
        String sql = "SELECT COUNT(*) FROM amazon_data WHERE sku = ? AND uacs = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, sku);
            stmt.setString(2, uacs);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean updateSellerLifeclIsc1UserIdDingAndSalesDepart(
            int id, String seller, String lifecl, String isc1, 
            String userIdDing, String salesDepart, int updateUserId) {
        String sql = "UPDATE amazon_data SET seller = ?, lifecl = ?, isc1 = ?, " +
                     "user_id_ding = ?, sales_depart = ?, update_user_id = ? WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, seller);
            stmt.setString(2, lifecl);
            stmt.setString(3, isc1);
            stmt.setString(4, userIdDing);
            stmt.setString(5, salesDepart);
            stmt.setInt(6, updateUserId);
            stmt.setInt(7, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 根据 ID 查询单条亚马逊数据（用于更新前获取原始值）
     */
    public AmazonData getOriginalData(int id) {
        String sql = "SELECT id, uacs, sku, seller, sales_depart, title, " +
                     "asin, parent_asin, lifecl, isc1, sku_last_date, user_id_ding, update_user_id " +
                     "FROM amazon_data WHERE id = ?";
        
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                AmazonData data = new AmazonData();
                data.setId(rs.getInt("id"));
                data.setUacs(rs.getString("uacs"));
                data.setSku(rs.getString("sku"));
                data.setSeller(rs.getString("seller"));
                data.setSalesDepart(rs.getString("sales_depart"));
                data.setTitle(rs.getString("title"));
                data.setAsin(rs.getString("asin"));
                data.setParentAsin(rs.getString("parent_asin"));
                data.setLifecl(rs.getString("lifecl"));
                data.setIsc1(rs.getString("isc1"));
                data.setSkuLastDate(rs.getDate("sku_last_date"));
                data.setUser_id_ding(rs.getString("user_id_ding"));
                // 如果需要 update_user_id，也可以设置
                return data;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // 未找到或出错
    }
    public List<AmazonData> getAmazonDataAfterId(
            String searchField, String keyword,
            String searchField2, String keyword2, // 新增第二个搜索条件
            boolean filterEmptySeller,
            Integer year,
            int lastId,
            int limit) {

        StringBuilder sql = new StringBuilder(
            "SELECT id, uacs, sku, seller, sales_depart, title, " +
            "asin, parent_asin, lifecl, isc1, sku_last_date, update_user_id " +
            "FROM amazon_data WHERE id > ?"
        );
        List<Object> params = new ArrayList<>();
        params.add(lastId);

        // === 复用原有条件逻辑（注意：必须和 getAmazonDataByPage 一致）===
        // 第一个搜索条件
        if (searchField != null && !searchField.isEmpty() && keyword != null && !keyword.isEmpty()) {
            List<String> allowedFields = Arrays.asList("id", "uacs", "sku", "seller", "sales_depart",
                    "title", "asin", "parent_asin", "lifecl", "isc1");
            if (allowedFields.contains(searchField)) {
                // 特殊处理部门字段：支持多值查询（逗号分隔）
                if ("sales_depart".equals(searchField)) {
                    if (keyword.contains(",")) {
                        // 多个部门代码，使用IN查询
                        String[] deptCodes = keyword.split(",");
                        StringBuilder inClause = new StringBuilder();
                        for (int i = 0; i < deptCodes.length; i++) {
                            if (i > 0) inClause.append(",");
                            inClause.append("?");
                            params.add(deptCodes[i].trim());
                        }
                        sql.append(" AND ").append(searchField).append(" IN (").append(inClause).append(")");
                    } else {
                        // 单个部门代码，使用等于查询
                        sql.append(" AND ").append(searchField).append(" = ?");
                        params.add(keyword.trim());
                    }
                } else {
                    // 其他字段正常LIKE查询
                    sql.append(" AND ").append(searchField).append(" LIKE ?");
                    params.add("%" + keyword + "%");
                }
            }
        }

        // 第二个搜索条件
        if (searchField2 != null && !searchField2.isEmpty() && keyword2 != null && !keyword2.isEmpty()) {
            List<String> allowedFields = Arrays.asList("id", "uacs", "sku", "seller", "sales_depart",
                    "title", "asin", "parent_asin", "lifecl", "isc1");
            if (allowedFields.contains(searchField2)) {
                // 特殊处理部门字段：支持多值查询（逗号分隔）
                if ("sales_depart".equals(searchField2)) {
                    if (keyword2.contains(",")) {
                        // 多个部门代码，使用IN查询
                        String[] deptCodes = keyword2.split(",");
                        StringBuilder inClause = new StringBuilder();
                        for (int i = 0; i < deptCodes.length; i++) {
                            if (i > 0) inClause.append(",");
                            inClause.append("?");
                            params.add(deptCodes[i].trim());
                        }
                        sql.append(" AND ").append(searchField2).append(" IN (").append(inClause).append(")");
                    } else {
                        // 单个部门代码，使用等于查询
                        sql.append(" AND ").append(searchField2).append(" = ?");
                        params.add(keyword2.trim());
                    }
                } else {
                    // 其他字段正常LIKE查询
                    sql.append(" AND ").append(searchField2).append(" LIKE ?");
                    params.add("%" + keyword2 + "%");
                }
            }
        }

        if (filterEmptySeller) {
            sql.append(" AND (seller IS NULL OR seller = '')");
        }

        if (year != null) {
            LocalDate start = LocalDate.of(year, 1, 1);
            LocalDate end = LocalDate.of(year + 1, 1, 1);
            sql.append(" AND sku_last_date >= ? AND sku_last_date < ?");
            params.add(Date.valueOf(start));
            params.add(Date.valueOf(end));
        }

        sql.append(" ORDER BY id ASC LIMIT ?");
        params.add(limit);

        // === 执行查询（复用原有 ResultSet 解析逻辑）===
        return executeQuery(sql.toString(), params);
    }
}
