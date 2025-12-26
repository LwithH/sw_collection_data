package com.servlet;

import com.util.DBUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/FileDownloadServlet")
public class FileDownloadServlet extends HttpServlet {

    // 可选：限制允许导出的表名（增强安全）
    private static final List<String> ALLOWED_DYNAMIC_TABLES = new ArrayList<>();

    static {
        // 示例：只允许这些表被动态导出
        // ALLOWED_DYNAMIC_TABLES.add("temu_data");
        // ALLOWED_DYNAMIC_TABLES.add("amazon_sales");
        // 如果留空，则允许任意合法表名（依赖前端校验）
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String idStr = request.getParameter("id");
        if (idStr == null || !idStr.matches("\\d+")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "无效的资源ID");
            return;
        }

        // 新增：获取日期范围参数
        String startDate = request.getParameter("startDate");
        String endDate = request.getParameter("endDate");

        int resourceId = Integer.parseInt(idStr);
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            // 查询资源元数据（包含 file_type 和日期范围配置）
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(
                "SELECT original_filename, file_path, resource_type, source_table_name, " +
                "file_type, date_range_enabled, date_field, default_start_date, default_end_date " +
                "FROM download_resources WHERE id = ?"
            );
            ps.setInt(1, resourceId);
            rs = ps.executeQuery();

            if (!rs.next()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "资源不存在");
                return;
            }

            String originalFilename = rs.getString("original_filename");
            String filePath = rs.getString("file_path");
            String resourceType = rs.getString("resource_type");
            String tableName = rs.getString("source_table_name");
            String fileType = rs.getString("file_type"); // 新增：获取格式
            boolean dateRangeEnabled = rs.getBoolean("date_range_enabled");
            String dateField = rs.getString("date_field");
            Date defaultStartDate = rs.getDate("default_start_date");
            Date defaultEndDate = rs.getDate("default_end_date");

            // 处理日期范围参数
            String finalStartDate = null;
            String finalEndDate = null;
            
            if (dateRangeEnabled) {
                // 如果用户提供了日期范围，使用用户提供的
                if (startDate != null && !startDate.trim().isEmpty() && 
                    endDate != null && !endDate.trim().isEmpty()) {
                    finalStartDate = startDate;
                    finalEndDate = endDate;
                } 
                // 否则使用默认日期范围
                else if (defaultStartDate != null && defaultEndDate != null) {
                    finalStartDate = defaultStartDate.toString();
                    finalEndDate = defaultEndDate.toString();
                }
                // 如果都没有，返回错误
                else {
                    response.setContentType("text/html");
                    response.getWriter().println(
                        "<html><body><h3>日期范围必填</h3>" +
                        "<p>此资源需要指定日期范围。请在前端页面选择开始日期和结束日期。</p>" +
                        "<a href='javascript:history.back()'>返回</a></body></html>"
                    );
                    return;
                }
                
                // 验证日期范围
                if (finalStartDate != null && finalEndDate != null && 
                    finalStartDate.compareTo(finalEndDate) > 0) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "开始日期不能晚于结束日期");
                    return;
                }
            }

            // ========== 处理动态资源（DYNAMIC）==========
            if ("DYNAMIC".equalsIgnoreCase(resourceType)) {
                if (tableName == null || tableName.trim().isEmpty()) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "动态资源缺少表名");
                    return;
                }

                // 安全校验
                if (!ALLOWED_DYNAMIC_TABLES.isEmpty() && 
                    !ALLOWED_DYNAMIC_TABLES.contains(tableName)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "该表不允许动态导出");
                    return;
                }

                // 根据格式生成内容
                if ("xlsx".equalsIgnoreCase(fileType)) {
                    byte[] excelBytes = generateExcelFromTable(tableName, dateField, finalStartDate, finalEndDate);
                    if (excelBytes == null) {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Excel 生成失败");
                        return;
                    }

                    // 设置 Excel 响应头
                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    String encodedFilename = URLEncoder.encode(originalFilename, "UTF-8")
                            .replaceAll("\\+", "%20");
                    response.setHeader("Content-Disposition", "attachment; filename=\"" +
                            encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);
                    response.setContentLength(excelBytes.length);

                    try (ServletOutputStream out = response.getOutputStream()) {
                        out.write(excelBytes);
                        out.flush();
                    }
                } else {
                    // 默认 CSV
                    String csvContent = generateCSVFromTable(tableName, dateField, finalStartDate, finalEndDate);
                    if (csvContent == null) {
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "CSV 生成失败");
                        return;
                    }

                    response.setContentType("text/csv;charset=UTF-8");
                    String encodedFilename = URLEncoder.encode(originalFilename, "UTF-8")
                            .replaceAll("\\+", "%20");
                    response.setHeader("Content-Disposition", "attachment; filename=\"" +
                            encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);

                    try (PrintWriter writer = response.getWriter()) {
                        writer.write("\uFEFF"); // UTF-8 BOM
                        writer.write(csvContent);
                    }
                }
                return;
            }

            // ========== 处理静态资源 ==========
            if (filePath == null || filePath.trim().isEmpty()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "静态文件路径缺失");
                return;
            }

            Path file = Paths.get(filePath);
            if (!Files.exists(file) || !Files.isReadable(file)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在或无法读取");
                return;
            }

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Length", String.valueOf(Files.size(file)));

            String encodedFilename = URLEncoder.encode(originalFilename, "UTF-8")
                    .replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "attachment; filename=\"" +
                    encodedFilename + "\"; filename*=UTF-8''" + encodedFilename);

            try (ServletOutputStream out = response.getOutputStream();
                 InputStream in = Files.newInputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
                out.flush();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "数据库错误");
        } finally {
            try {
                if (rs != null) rs.close();
                if (ps != null) ps.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // 修改Excel生成方法，添加日期范围过滤
    private byte[] generateExcelFromTable(String tableName, String dateField, String startDate, String endDate) {
        // 限制最大行数（安全兜底）
        final int MAX_ROWS = 1000000; // 最多 200 万行

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
            
            // 添加行数限制
            sqlBuilder.append(" LIMIT ").append(MAX_ROWS);
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlBuilder.toString());

            // 使用 SXSSFWorkbook（流式写入）
            SXSSFWorkbook workbook = new SXSSFWorkbook(100); // 100 行缓存在内存
            Sheet sheet = workbook.createSheet("Data");

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 创建标题行
            Row headerRow = sheet.createRow(0);
            for (int i = 1; i <= columnCount; i++) {
                Cell cell = headerRow.createCell(i - 1);
                cell.setCellValue(metaData.getColumnName(i));
            }

            // 填充数据（流式）
            int rowNum = 1;
            while (rs.next()) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 1; i <= columnCount; i++) {
                    Cell cell = row.createCell(i - 1);
                    String value = rs.getString(i);
                    if (value != null) {
                        cell.setCellValue(value);
                    }
                }
                // 可选：每 1 万行打印日志（调试用）
                if (rowNum % 10000 == 0) System.out.println("已处理 " + rowNum + " 行");
            }

            // 写入字节数组
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            // 清理临时文件（重要！）
            workbook.dispose();
            workbook.close();

            return out.toByteArray();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try { if (rs != null) rs.close(); } catch (Exception e) { /* ignored */ }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { /* ignored */ }
            try { if (conn != null) conn.close(); } catch (Exception e) { /* ignored */ }
        }
    }

    // 修改CSV生成方法，添加日期范围过滤
    private String generateCSVFromTable(String tableName, String dateField, String startDate, String endDate) throws SQLException {
        StringBuilder csv = new StringBuilder();
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
            sqlBuilder.append(" LIMIT 50000");
            
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sqlBuilder.toString());

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // 标题
            for (int i = 1; i <= columnCount; i++) {
                csv.append(metaData.getColumnName(i));
                if (i < columnCount) csv.append(",");
            }
            csv.append("\n");

            // 数据
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String value = rs.getString(i);
                    if (value != null) {
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
            try { if (rs != null) rs.close(); } catch (Exception e) { /* ignored */ }
            try { if (stmt != null) stmt.close(); } catch (Exception e) { /* ignored */ }
            try { if (conn != null) conn.close(); } catch (Exception e) { /* ignored */ }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
