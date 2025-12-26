package com.servlet;

import com.model.User;
import com.util.DBUtil;
import com.util.FileUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@WebServlet("/DownloadCenterAdminServlet")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, // 1MB
                 maxFileSize = 50 * 1024 * 1024,    // 50MB
                 maxRequestSize = 50 * 1024 * 1024) // 50MB
public class DownloadCenterAdminServlet extends HttpServlet {

    private static final String UPLOAD_DIR = "WEB-INF/uploads";
    private static final long MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB
    private static final String[] ALLOWED_EXTENSIONS = {".xlsx", ".xls", ".csv", ".zip", ".doc", ".docx"};
    private static final int MAX_ESTIMATE_ROWS = 10000; // 估算最大行数
    private static final long MIN_ESTIMATED_SIZE = 1024; // 最小估算大小 1KB
    private static final long MAX_ESTIMATED_SIZE = 50 * 1024 * 1024; // 最大估算大小 50MB

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        User user = (User) request.getSession().getAttribute("loginUser");

        if (user == null || !hasDownloadAdminPermission(user)) {
            response.sendRedirect("index.jsp");
            return;
        }
        try {
            if ("uploadForm".equals(action)) {
                uploadForm(request, response);
            } else if ("manage".equals(action)) {
                manageResources(request, response, user);
            } else if ("delete".equals(action)) {
                deleteResource(request, response);
            } else if ("updateVisibility".equals(action)) {
                updateVisibility(request, response);
            } else if ("editPermissions".equals(action)) {
                editPermissions(request, response);
            } else {
                response.sendRedirect("index.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "操作失败: " + e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        User user = (User) request.getSession().getAttribute("loginUser");

        if (user == null || !hasDownloadAdminPermission(user)) {
            response.sendRedirect("index.jsp");
            return;
        }

        try {
            if ("doUploadLocal".equals(action)) {
                doUploadLocal(request, response, user);
            } else if ("doExportTable".equals(action)) {
                doExportTable(request, response, user);
            } else if ("updateVisibility".equals(action)) {
                updateVisibility(request, response);
            } else if ("savePermissions".equals(action)) {
                savePermissions(request, response);
            } else {
                response.sendRedirect("index.jsp");
            }
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("message", "操作失败: " + e.getMessage());
            request.getRequestDispatcher("error.jsp").forward(request, response);
        }
    }

    // ========== 辅助方法 ==========

    private boolean hasDownloadAdminPermission(User user) {
        String downloadAdmin = user.getIsDownloadAdmin();
        String admin = user.getIsAdmin();
        return "yes".equalsIgnoreCase(downloadAdmin) ||
               ("yes".equalsIgnoreCase(admin) && (downloadAdmin == null || downloadAdmin.trim().isEmpty()));
    }

    private void uploadForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        // 获取所有部门和用户，用于权限设置
        List<String> departments = getAllDepartments();
        List<User> allUsers = getAllUsers();
        
        request.setAttribute("departments", departments);
        request.setAttribute("allUsers", allUsers);
        request.getRequestDispatcher("/upload_form.jsp").forward(request, response);
    }

    private void manageResources(HttpServletRequest request, HttpServletResponse response, User user)
            throws SQLException, ServletException, IOException {
        List<Resource> resources = getAllResources();
        // 获取每个资源的权限信息
        for (Resource r : resources) {
            r.setPermissions(getResourcePermissions(r.getId()));
        }
        request.setAttribute("resources", resources);
        request.getRequestDispatcher("/manage_resources.jsp").forward(request, response);
    }

    private void doUploadLocal(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException, SQLException {
        Part filePart = request.getPart("file");
        String name = request.getParameter("name");
        String category = request.getParameter("category");
        String description = request.getParameter("description");
        String[] allowedDepartments = request.getParameterValues("allowed_departments[]");
        String[] allowedUsers = request.getParameterValues("allowed_users[]");

        if (filePart == null || filePart.getSize() == 0) {
            request.setAttribute("message", "请选择文件！");
            uploadForm(request, response);
            return;
        }

        if (filePart.getSize() > MAX_FILE_SIZE) {
            request.setAttribute("message", "文件大小不能超过50MB！");
            uploadForm(request, response);
            return;
        }

        String originalFileName = getFileName(filePart);
        String ext = "";
        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalFileName.substring(dotIndex).toLowerCase();
        }

        boolean allowed = false;
        for (String allowedExt : ALLOWED_EXTENSIONS) {
            if (allowedExt.equals(ext)) {
                allowed = true;
                break;
            }
        }
        if (!allowed) {
            request.setAttribute("message", "仅允许上传 .xlsx, .xls, .csv, .zip, .doc, .docx 文件！");
            uploadForm(request, response);
            return;
        }

        // 生成唯一文件名
        String uniqueName = FileUtil.generateUniqueFileName(originalFileName);
        String savePath = getServletContext().getRealPath("/") + UPLOAD_DIR;
        Path uploadDir = Paths.get(savePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path filePath = uploadDir.resolve(uniqueName);

        // 保存文件
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, filePath);
        }

        // 保存元数据到数据库 - 返回资源ID（本地文件上传不支持日期范围）
        int resourceId = saveResourceToDB(name, category, description, "LOCAL", 
                         filePath.toString(), originalFileName,
                         filePart.getSize(), ext, null, null,
                         user.getId(), user.getUsername(),
                         false, null, null, null);
        
        // 保存权限设置
        if (allowedDepartments != null || allowedUsers != null) {
            saveResourcePermissions(resourceId, allowedDepartments, allowedUsers);
        }

        response.sendRedirect("DownloadCenterAdminServlet?action=manage");
    }

    // 检查数据库中是否存在指定表（防无效表名）
    private boolean tableExists(String tableName) throws SQLException {
        try (Connection conn = DBUtil.getConnection();
             ResultSet rs = conn.getMetaData().getTables(null, null, tableName, new String[]{"TABLE"})) {
            return rs.next();
        }
    }

    // ✅ 新增：检查表是否有日期字段
    private boolean hasDateField(String tableName, String dateField) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT " + dateField + " FROM `" + tableName + "` LIMIT 1";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            return true; // 如果执行成功，说明字段存在
        } catch (SQLException e) {
            // 如果字段不存在，会抛出SQL异常
            return false;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { /* ignored */ }
            try { if (ps != null) ps.close(); } catch (Exception e) { /* ignored */ }
            try { if (conn != null) conn.close(); } catch (Exception e) { /* ignored */ }
        }
    }

    private void doExportTable(HttpServletRequest request, HttpServletResponse response, User user)
            throws ServletException, IOException, SQLException {
        String tableName = request.getParameter("tableName");
        String name = request.getParameter("name");
        String category = request.getParameter("category");
        String description = request.getParameter("description");
        String isDynamicParam = request.getParameter("isDynamic");
        String fileType = request.getParameter("fileType");
        String[] allowedDepartments = request.getParameterValues("allowed_departments[]");
        String[] allowedUsers = request.getParameterValues("allowed_users[]");
        
        // 新增：日期范围相关参数
        String dateRangeEnabledParam = request.getParameter("dateRangeEnabled");
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");
        String dateFieldParam = request.getParameter("dateField"); // 可选，可以默认"date"

        // 默认为 csv
        if (fileType == null || (!"csv".equals(fileType) && !"xlsx".equals(fileType))) {
            fileType = "csv";
        }

        boolean isDynamic = "true".equals(isDynamicParam);
        boolean dateRangeEnabled = "true".equals(dateRangeEnabledParam);
        String dateField = (dateFieldParam != null && !dateFieldParam.trim().isEmpty()) ? 
                          dateFieldParam.trim() : "date"; // 默认日期字段名为"date"

        if (tableName == null || tableName.trim().isEmpty()) {
            request.setAttribute("message", "表名不能为空！");
            uploadForm(request, response);
            return;
        }

        if (!tableName.matches("^[a-zA-Z_][a-zA-Z0-9_]*$")) {
            request.setAttribute("message", "表名格式非法！");
            uploadForm(request, response);
            return;
        }

        // 检查表是否存在且包含日期字段（如果启用了日期范围）
        if (dateRangeEnabled && !hasDateField(tableName, dateField)) {
            request.setAttribute("message", "表 '" + tableName + "' 中没有找到日期字段 '" + dateField + "'！");
            uploadForm(request, response);
            return;
        }

        if (isDynamic) {
            if (!tableExists(tableName)) {
                request.setAttribute("message", "数据库中不存在表 '" + tableName + "'！");
                uploadForm(request, response);
                return;
            }

            // ✅ 修复：为动态Excel和CSV提供智能估算
            long estimatedSize = 0;
            if ("csv".equals(fileType)) {
                String csvContent = exportTableToCSV(tableName, false, null, null, null); // 不包含BOM
                if (csvContent != null) {
                    // 计算带BOM的UTF-8大小 (3字节BOM + 内容字节)
                    byte[] bomBytes = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
                    byte[] contentBytes = csvContent.getBytes("UTF-8");
                    estimatedSize = bomBytes.length + contentBytes.length;
                }
            } else if ("xlsx".equals(fileType) || "xls".equals(fileType)) {
                // ✅ 为动态Excel提供估算大小
                estimatedSize = estimateExcelSize(tableName);
            }
            
            // 保存资源 - 返回资源ID
            int resourceId = saveResourceToDB(name, category, description, "DYNAMIC",
                             null, name + "." + fileType, estimatedSize, fileType, tableName, null,
                             user.getId(), user.getUsername(),
                             dateRangeEnabled, dateField, startDate, endDate);
            
            // 保存权限设置
            if (allowedDepartments != null || allowedUsers != null) {
                saveResourcePermissions(resourceId, allowedDepartments, allowedUsers);
            }

            response.sendRedirect("DownloadCenterAdminServlet?action=manage");
            return;
        }

        // ========== 静态资源：生成文件 ==========
        if ("xlsx".equals(fileType)) {
            request.setAttribute("message", "静态资源暂不支持 Excel 格式，请使用动态导出。");
            uploadForm(request, response);
            return;
        }

        // 修复CSV BOM处理
        String csvContent = exportTableToCSV(tableName, false, dateField, startDate, endDate); // 不包含BOM
        if (csvContent == null) {
            request.setAttribute("message", "导出失败：表不存在或无数据");
            uploadForm(request, response);
            return;
        }

        String originalFileName = name + ".csv";
        String uniqueName = FileUtil.generateUniqueFileName(originalFileName);
        String savePath = getServletContext().getRealPath("/") + UPLOAD_DIR;
        Path uploadDir = Paths.get(savePath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
        Path filePath = uploadDir.resolve(uniqueName);

        // 正确写入带BOM的CSV文件
        byte[] bomBytes = new byte[]{(byte)0xEF, (byte)0xBB, (byte)0xBF};
        byte[] contentBytes = csvContent.getBytes("UTF-8");
        byte[] fullContent = new byte[bomBytes.length + contentBytes.length];
        System.arraycopy(bomBytes, 0, fullContent, 0, bomBytes.length);
        System.arraycopy(contentBytes, 0, fullContent, bomBytes.length, contentBytes.length);
        
        Files.write(filePath, fullContent, 
                    StandardOpenOption.CREATE, StandardOpenOption.WRITE);

        // 获取实际文件大小
        long actualSize = Files.size(filePath);
        
        // 保存资源 - 返回资源ID
        int resourceId = saveResourceToDB(name, category, description, "STATIC",
                         filePath.toString(), originalFileName,
                         actualSize, "csv", tableName, null,
                         user.getId(), user.getUsername(),
                         dateRangeEnabled, dateField, startDate, endDate);
        
        // 保存权限设置
        if (allowedDepartments != null || allowedUsers != null) {
            saveResourcePermissions(resourceId, allowedDepartments, allowedUsers);
        }

        response.sendRedirect("DownloadCenterAdminServlet?action=manage");
    }

    // ✅ 新增：Excel文件大小估算方法（安全且快速）
    private long estimateExcelSize(String tableName) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // 1. 获取安全行数（带LIMIT）
            int rowCount;
            String countSql = "SELECT COUNT(*) AS cnt FROM `" + tableName + "` LIMIT " + (MAX_ESTIMATE_ROWS + 1);
            ps = conn.prepareStatement(countSql);
            rs = ps.executeQuery();
            rs.next();
            rowCount = rs.getInt("cnt");
            
            // 超过阈值直接返回最大值
            if (rowCount > MAX_ESTIMATE_ROWS) {
                return MAX_ESTIMATED_SIZE;
            }
            
            // 2. 获取列数
            int columnCount;
            String metaSql = "SELECT * FROM `" + tableName + "` LIMIT 1";
            try (Statement stmt = conn.createStatement();
                 ResultSet metaRs = stmt.executeQuery(metaSql)) {
                ResultSetMetaData metaData = metaRs.getMetaData();
                columnCount = metaData.getColumnCount();
            }
            
            // 3. 智能估算公式：基础开销(5KB) + 行×列×每单元格8字节
            long estimated = 5000L + (long) rowCount * columnCount * 8;
            
            // 4. 应用安全边界
            return Math.max(MIN_ESTIMATED_SIZE, Math.min(estimated, MAX_ESTIMATED_SIZE));
            
        } catch (Exception e) {
            e.printStackTrace();
            return MIN_ESTIMATED_SIZE; // 出错时返回最小值
        } finally {
            // 安全关闭资源
            try { if (rs != null) rs.close(); } catch (Exception e) { /* ignored */ }
            try { if (ps != null) ps.close(); } catch (Exception e) { /* ignored */ }
            try { if (conn != null) conn.close(); } catch (Exception e) { /* ignored */ }
        }
    }

    // 修复方法：添加includeBOM参数控制BOM头，添加日期范围参数
    private String exportTableToCSV(String tableName, boolean includeBOM, 
                                   String dateField, String startDate, String endDate) throws SQLException {
        StringBuilder csv = new StringBuilder();
        
        // 仅当明确要求时才添加BOM
        if (includeBOM) {
            csv.append("\uFEFF");
        }
        
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = DBUtil.getConnection();
            
            // 构建SQL查询（带日期范围条件）
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM `" + tableName + "`");
            
            // 添加日期范围条件
            if (dateField != null && startDate != null && endDate != null) {
                sqlBuilder.append(" WHERE `").append(dateField).append("` >= '").append(startDate).append("'")
                         .append(" AND `").append(dateField).append("` <= '").append(endDate).append("'");
            }
            
            // 防止过大，添加限制
            sqlBuilder.append(" LIMIT 10000");
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlBuilder.toString());

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 写入标题行
            for (int i = 1; i <= columnCount; i++) {
                csv.append(metaData.getColumnName(i));
                if (i < columnCount) csv.append(",");
            }
            csv.append("\n");

            // 写入数据行
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value != null) {
                        // 处理 CSV 特殊字符
                        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
                            value = "\"" + value.replace("\"", "\"\"") + "\"";
                        }
                    } else {
                        value = "";
                    }
                    csv.append(value);
                    if (i < columnCount) csv.append(",");
                }
                csv.append("\n");
            }

            return csv.toString();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { /* ignored */ }
            if (stmt != null) try { stmt.close(); } catch (SQLException e) { /* ignored */ }
            if (conn != null) try { conn.close(); } catch (SQLException e) { /* ignored */ }
        }
    }

    private void deleteResource(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        
        // 先查完整资源信息（包含 resource_type）
        Resource resource = getResourceById(id);
        if (resource == null) {
            response.sendRedirect("DownloadCenterAdminServlet?action=manage");
            return;
        }

        // ========== 只有静态资源才删除物理文件 ==========
        if ("LOCAL".equals(resource.getResourceType()) || "STATIC".equals(resource.getResourceType())) {
            String filePathStr = resource.getFilePath();
            if (filePathStr != null && !filePathStr.trim().isEmpty()) {
                Path filePath = Paths.get(filePathStr);
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            }
        }
        // 动态资源（DYNAMIC）无需删除文件

        // 删除数据库记录（权限表记录会因外键约束自动删除）
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM download_resources WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }

        response.sendRedirect("DownloadCenterAdminServlet?action=manage");
    }

    private void updateVisibility(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int id = Integer.parseInt(request.getParameter("id"));
        String isVisible = request.getParameter("isVisible");

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE download_resources SET is_visible = ? WHERE id = ?")) {
            ps.setString(1, isVisible);
            ps.setInt(2, id);
            ps.executeUpdate();
        }

        response.sendRedirect("DownloadCenterAdminServlet?action=manage");
    }

    // ========== 权限相关方法 ==========
    
    private void editPermissions(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException, SQLException {
        int resourceId = Integer.parseInt(request.getParameter("id"));
        
        // 获取资源信息
        Resource resource = getResourceById(resourceId);
        if (resource == null) {
            response.sendRedirect("DownloadCenterAdminServlet?action=manage");
            return;
        }
        
        // 获取当前权限设置
        List<Permission> permissions = getResourcePermissions(resourceId);
        
        // 获取所有部门和用户
        List<String> departments = getAllDepartments();
        List<User> allUsers = getAllUsers();
        
        request.setAttribute("resource", resource);
        request.setAttribute("permissions", permissions);
        request.setAttribute("departments", departments);
        request.setAttribute("allUsers", allUsers);
        request.getRequestDispatcher("/edit_permissions.jsp").forward(request, response);
    }
    
    private void savePermissions(HttpServletRequest request, HttpServletResponse response)
            throws SQLException, IOException {
        int resourceId = Integer.parseInt(request.getParameter("resourceId"));
        String[] allowedDepartments = request.getParameterValues("allowed_departments[]");
        String[] allowedUsers = request.getParameterValues("allowed_users[]");
        
        // 先删除旧的权限记录
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM download_resource_permissions WHERE resource_id = ?")) {
            ps.setInt(1, resourceId);
            ps.executeUpdate();
        }
        
        // 保存新的权限设置
        if (allowedDepartments != null || allowedUsers != null) {
            saveResourcePermissions(resourceId, allowedDepartments, allowedUsers);
        }
        
        response.sendRedirect("DownloadCenterAdminServlet?action=manage");
    }
    
    private void saveResourcePermissions(int resourceId, String[] departments, String[] userIds) 
            throws SQLException {
        String sql = "INSERT INTO download_resource_permissions (resource_id, target_type, target_value) VALUES (?, ?, ?)";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (departments != null) {
                for (String dept : departments) {
                    if (dept != null && !dept.trim().isEmpty()) {
                        ps.setInt(1, resourceId);
                        ps.setString(2, "DEPARTMENT");
                        ps.setString(3, dept.trim());
                        ps.addBatch();
                    }
                }
            }
            if (userIds != null) {
                for (String userId : userIds) {
                    if (userId != null && !userId.trim().isEmpty()) {
                        ps.setInt(1, resourceId);
                        ps.setString(2, "USER");
                        ps.setString(3, userId.trim());
                        ps.addBatch();
                    }
                }
            }
            ps.executeBatch();
        }
    }
    
    private List<Permission> getResourcePermissions(int resourceId) throws SQLException {
        List<Permission> permissions = new ArrayList<>();
        String sql = "SELECT * FROM download_resource_permissions WHERE resource_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, resourceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Permission p = new Permission();
                    p.setId(rs.getInt("id"));
                    p.setResourceId(rs.getInt("resource_id"));
                    p.setTargetType(rs.getString("target_type"));
                    p.setTargetValue(rs.getString("target_value"));
                    permissions.add(p);
                }
            }
        }
        return permissions;
    }
    
    // 获取所有部门（去重）
    private List<String> getAllDepartments() throws SQLException {
        List<String> departments = new ArrayList<>();
        String sql = "SELECT DISTINCT user_depart FROM user WHERE user_depart IS NOT NULL AND user_depart != '' ORDER BY user_depart";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                departments.add(rs.getString("user_depart"));
            }
        }
        return departments;
    }
    
    // 获取所有用户
    private List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, name, user_depart FROM user ORDER BY user_depart, name";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setUsername(rs.getString("username"));
                user.setName(rs.getString("name"));  // 新增name字段
                user.setUserDepart(rs.getString("user_depart"));
                users.add(user);
            }
        }
        return users;
    }

    // ========== 数据库操作 ==========

    // ✅ 新增：修改保存方法，添加日期范围字段
    private int saveResourceToDB(String name, String category, String description,
                                  String resourceType, String filePath, String originalFileName,
                                  long fileSize, String fileType, String sourceTable, String queryCondition,
                                  int uploaderId, String uploaderName,
                                  boolean dateRangeEnabled, String dateField, 
                                  String startDate, String endDate) throws SQLException {
        String sql = "INSERT INTO download_resources " +
                "(name, category, description, file_path, original_filename, file_size, file_type, " +
                "resource_type, source_table_name, query_condition, is_visible, uploader_id, uploader_name, " +
                "date_range_enabled, date_field, default_start_date, default_end_date, upload_time) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'yes', ?, ?, ?, ?, ?, ?, NOW())";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            int index = 1;
            ps.setString(index++, name);
            ps.setString(index++, category);
            ps.setString(index++, description);
            ps.setString(index++, filePath);
            ps.setString(index++, originalFileName);
            ps.setLong(index++, fileSize);
            ps.setString(index++, fileType);
            ps.setString(index++, resourceType);
            ps.setString(index++, sourceTable);
            ps.setString(index++, queryCondition);
            ps.setInt(index++, uploaderId);
            ps.setString(index++, uploaderName);
            ps.setBoolean(index++, dateRangeEnabled);
            ps.setString(index++, dateField);
            
            // 设置日期参数（可能为null）
            if (startDate != null && !startDate.trim().isEmpty()) {
                ps.setDate(index++, Date.valueOf(startDate));
            } else {
                ps.setNull(index++, Types.DATE);
            }
            
            if (endDate != null && !endDate.trim().isEmpty()) {
                ps.setDate(index++, Date.valueOf(endDate));
            } else {
                ps.setNull(index++, Types.DATE);
            }
            
            ps.executeUpdate();
            
            // 返回生成的ID
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("创建资源失败，无法获取ID");
                }
            }
        }
    }

    // 修改查询方法，包含新字段
    private List<Resource> getAllResources() throws SQLException {
        List<Resource> list = new ArrayList<>();
        String sql = "SELECT * FROM download_resources ORDER BY upload_time DESC";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                Resource r = new Resource();
                r.setId(rs.getInt("id"));
                r.setName(rs.getString("name"));
                r.setCategory(rs.getString("category"));
                r.setDescription(rs.getString("description"));
                r.setFilePath(rs.getString("file_path"));
                r.setOriginalFilename(rs.getString("original_filename"));
                r.setFileSize(rs.getLong("file_size"));
                r.setFileType(rs.getString("file_type"));
                r.setResourceType(rs.getString("resource_type"));
                r.setSourceTableName(rs.getString("source_table_name"));
                r.setIsVisible(rs.getString("is_visible"));
                r.setUploaderName(rs.getString("uploader_name"));
                r.setUploadTime(rs.getTimestamp("upload_time"));
                
                // 新增字段
                r.setDateRangeEnabled(rs.getBoolean("date_range_enabled"));
                r.setDateField(rs.getString("date_field"));
                r.setDefaultStartDate(rs.getDate("default_start_date"));
                r.setDefaultEndDate(rs.getDate("default_end_date"));
                
                list.add(r);
            }
        }
        return list;
    }

    // 修改查询方法，包含新字段
    private Resource getResourceById(int id) throws SQLException {
        String sql = "SELECT * FROM download_resources WHERE id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Resource r = new Resource();
                    r.setId(rs.getInt("id"));
                    r.setName(rs.getString("name"));
                    r.setCategory(rs.getString("category"));
                    r.setDescription(rs.getString("description"));
                    r.setFilePath(rs.getString("file_path"));
                    r.setOriginalFilename(rs.getString("original_filename"));
                    r.setFileSize(rs.getLong("file_size"));
                    r.setFileType(rs.getString("file_type"));
                    r.setResourceType(rs.getString("resource_type"));
                    r.setSourceTableName(rs.getString("source_table_name"));
                    r.setIsVisible(rs.getString("is_visible"));
                    r.setUploaderName(rs.getString("uploader_name"));
                    r.setUploadTime(rs.getTimestamp("upload_time"));
                    
                    // 新增字段
                    r.setDateRangeEnabled(rs.getBoolean("date_range_enabled"));
                    r.setDateField(rs.getString("date_field"));
                    r.setDefaultStartDate(rs.getDate("default_start_date"));
                    r.setDefaultEndDate(rs.getDate("default_end_date"));
                    
                    return r;
                }
            }
        }
        return null;
    }

    // ========== 工具方法 ==========

    private String getFileName(Part part) {
        String contentDisp = part.getHeader("content-disposition");
        if (contentDisp != null) {
            for (String token : contentDisp.split(";")) {
                token = token.trim();
                if (token.startsWith("filename")) {
                    return token.substring(token.indexOf('=') + 1).replace("\"", "");
                }
            }
        }
        return "unknown";
    }

    // ========== 内部类 ==========

    // 内部类：资源模型（添加日期范围字段）
    public static class Resource {
        private int id;
        private String name;
        private String category;
        private String description;
        private String filePath;
        private String originalFilename;
        private long fileSize;
        private String fileType;
        private String resourceType; // LOCAL / STATIC / DYNAMIC
        private String sourceTableName;
        private String isVisible;
        private String uploaderName;
        private Timestamp uploadTime;
        private List<Permission> permissions;
        
        // 新增字段
        private boolean dateRangeEnabled;
        private String dateField;
        private Date defaultStartDate;
        private Date defaultEndDate;

        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getFilePath() { return filePath; }
        public void setFilePath(String filePath) { this.filePath = filePath; }
        public String getOriginalFilename() { return originalFilename; }
        public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
        public long getFileSize() { return fileSize; }
        public void setFileSize(long fileSize) { this.fileSize = fileSize; }
        public String getFileType() { return fileType; }
        public void setFileType(String fileType) { this.fileType = fileType; }
        public String getResourceType() { return resourceType; }
        public void setResourceType(String resourceType) { this.resourceType = resourceType; }
        public String getSourceTableName() { return sourceTableName; }
        public void setSourceTableName(String sourceTableName) { this.sourceTableName = sourceTableName; }
        public String getIsVisible() { return isVisible; }
        public void setIsVisible(String isVisible) { this.isVisible = isVisible; }
        public String getUploaderName() { return uploaderName; }
        public void setUploaderName(String uploaderName) { this.uploaderName = uploaderName; }
        public Timestamp getUploadTime() { return uploadTime; }
        public void setUploadTime(Timestamp uploadTime) { this.uploadTime = uploadTime; }
        public List<Permission> getPermissions() { return permissions; }
        public void setPermissions(List<Permission> permissions) { this.permissions = permissions; }
        
        // 新增字段的getter/setter
        public boolean isDateRangeEnabled() { return dateRangeEnabled; }
        public void setDateRangeEnabled(boolean dateRangeEnabled) { this.dateRangeEnabled = dateRangeEnabled; }
        public String getDateField() { return dateField; }
        public void setDateField(String dateField) { this.dateField = dateField; }
        public Date getDefaultStartDate() { return defaultStartDate; }
        public void setDefaultStartDate(Date defaultStartDate) { this.defaultStartDate = defaultStartDate; }
        public Date getDefaultEndDate() { return defaultEndDate; }
        public void setDefaultEndDate(Date defaultEndDate) { this.defaultEndDate = defaultEndDate; }
    }
    
    // 内部类：权限模型（保持不变）
    public static class Permission {
        private int id;
        private int resourceId;
        private String targetType; // DEPARTMENT 或 USER
        private String targetValue;
        
        // Getters and Setters
        public int getId() { return id; }
        public void setId(int id) { this.id = id; }
        public int getResourceId() { return resourceId; }
        public void setResourceId(int resourceId) { this.resourceId = resourceId; }
        public String getTargetType() { return targetType; }
        public void setTargetType(String targetType) { this.targetType = targetType; }
        public String getTargetValue() { return targetValue; }
        public void setTargetValue(String targetValue) { this.targetValue = targetValue; }
    }
}
