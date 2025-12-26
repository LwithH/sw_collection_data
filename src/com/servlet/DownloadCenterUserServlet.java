package com.servlet;

import com.model.User;
import com.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/DownloadCenterUserServlet")
public class DownloadCenterUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        User user = (User) request.getSession().getAttribute("loginUser");
        if (user == null) {
            response.sendRedirect("index.jsp");
            return;
        }

        String nameFilter = request.getParameter("name");
        String categoryFilter = request.getParameter("category");

        try {
            List<DownloadResource> resources = getResources(nameFilter, categoryFilter, user);
            request.setAttribute("resources", resources);
            request.setAttribute("nameFilter", nameFilter);
            request.setAttribute("categoryFilter", categoryFilter);
            request.getRequestDispatcher("/download_center.jsp").forward(request, response);
        } catch (SQLException e) {
            e.printStackTrace();
            request.setAttribute("message", "加载资源失败");
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    private List<DownloadResource> getResources(String name, String category, User currentUser) throws SQLException {
        List<DownloadResource> list = new ArrayList<>();
        
        // 复杂的权限查询：资源可见且（无权限记录 OR 用户部门匹配 OR 用户ID匹配）
        // 新增字段：date_range_enabled, date_field, default_start_date, default_end_date, resource_type
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT dr.id, dr.name, dr.category, dr.description, " +
            "dr.original_filename, dr.file_size, dr.upload_time, dr.uploader_name, " +
            "dr.date_range_enabled, dr.date_field, dr.default_start_date, dr.default_end_date, " +
            "dr.resource_type, dr.source_table_name " +
            "FROM download_resources dr " +
            "LEFT JOIN download_resource_permissions dp ON dr.id = dp.resource_id " +
            "WHERE dr.is_visible = 'yes' " +
            "AND (dp.resource_id IS NULL " + // 无权限记录 → 所有人可见
            "OR (dp.target_type = 'DEPARTMENT' AND dp.target_value = ?) " + // 部门匹配
            "OR (dp.target_type = 'USER' AND dp.target_value = ?))" // 用户ID匹配
        );

        // 添加筛选条件
        if (name != null && !name.trim().isEmpty()) {
            sql.append(" AND dr.name LIKE ?");
        }
        if (category != null && !category.trim().isEmpty()) {
            sql.append(" AND dr.category = ?");
        }
        sql.append(" ORDER BY dr.upload_time DESC");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            int index = 1;
            // 处理用户部门可能为null的情况
            String userDepart = currentUser.getUserDepart();
            if (userDepart == null) userDepart = "";
            ps.setString(index++, userDepart);
            ps.setString(index++, String.valueOf(currentUser.getId()));
            
            if (name != null && !name.trim().isEmpty()) {
                ps.setString(index++, "%" + name.trim() + "%");
            }
            if (category != null && !category.trim().isEmpty()) {
                ps.setString(index, category.trim());
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DownloadResource r = new DownloadResource();
                    r.setId(rs.getInt("id"));
                    r.setName(rs.getString("name"));
                    r.setCategory(rs.getString("category"));
                    r.setDescription(rs.getString("description"));
                    r.setOriginalFilename(rs.getString("original_filename"));
                    r.setFileSize(rs.getLong("file_size"));
                    r.setUploadTime(rs.getTimestamp("upload_time"));
                    r.setUploaderName(rs.getString("uploader_name"));
                    
                    // 新增字段
                    r.setDateRangeEnabled(rs.getBoolean("date_range_enabled"));
                    r.setDateField(rs.getString("date_field"));
                    r.setDefaultStartDate(rs.getDate("default_start_date"));
                    r.setDefaultEndDate(rs.getDate("default_end_date"));
                    // 判断是否为动态资源：如果resource_type为DYNAMIC
                    r.setDynamic("DYNAMIC".equals(rs.getString("resource_type")));
                    r.setSourceTableName(rs.getString("source_table_name"));
                    
                    list.add(r);
                }
            }
        }
        return list;
    }

    // ========== 内部 DTO 类：下载资源数据载体 ==========
    public static class DownloadResource {
        private int id;
        private String name;
        private String category;
        private String description;
        private String originalFilename;
        private long fileSize;
        private Timestamp uploadTime;
        private String uploaderName;
        
        // 新增字段
        private boolean dateRangeEnabled;
        private String dateField;
        private Date defaultStartDate;
        private Date defaultEndDate;
        private boolean isDynamic;
        private String sourceTableName;

        // ---------- Getters ----------
        public int getId() { return id; }
        public String getName() { return name; }
        public String getCategory() { return category; }
        public String getDescription() { return description; }
        public String getOriginalFilename() { return originalFilename; }
        public long getFileSize() { return fileSize; }
        public Timestamp getUploadTime() { return uploadTime; }
        public String getUploaderName() { return uploaderName; }
        
        // 新增字段的getter
        public boolean isDateRangeEnabled() { return dateRangeEnabled; }
        public String getDateField() { return dateField; }
        public Date getDefaultStartDate() { return defaultStartDate; }
        public Date getDefaultEndDate() { return defaultEndDate; }
        public boolean isDynamic() { return isDynamic; }
        public String getSourceTableName() { return sourceTableName; }

        // ---------- Setters ----------
        public void setId(int id) { this.id = id; }
        public void setName(String name) { this.name = name; }
        public void setCategory(String category) { this.category = category; }
        public void setDescription(String description) { this.description = description; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public void setUploadTime(Timestamp uploadTime) { this.uploadTime = uploadTime; }
        public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
        
        // 新增字段的setter
        public void setDateRangeEnabled(boolean dateRangeEnabled) { this.dateRangeEnabled = dateRangeEnabled; }
        public void setDateField(String dateField) { this.dateField = dateField; }
        public void setDefaultStartDate(Date defaultStartDate) { this.defaultStartDate = defaultStartDate; }
        public void setDefaultEndDate(Date defaultEndDate) { this.defaultEndDate = defaultEndDate; }
        public void setDynamic(boolean dynamic) { isDynamic = dynamic; }
        public void setSourceTableName(String sourceTableName) { this.sourceTableName = sourceTableName; }
    }
}
