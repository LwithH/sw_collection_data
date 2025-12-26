package com.servlet;//导出类   temu表导出代码

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.model.CollectionData;
import com.servlet.DataService; // 使用我们刚创建的服务层

@WebServlet("/ExportDataServlet")
public class ExportDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 1. 获取查询参数
        String keyword = request.getParameter("keyword");
        String filter = request.getParameter("filter"); // 获取filter参数
        
        try {
            DataService dataService = new DataService();
            List<CollectionData> exportData;
            
            // 2. 根据filter参数决定查询方式（适配SearchModifyServlet的逻辑）
            if ("emptySeller".equals(filter)) {
                exportData = dataService.getEmptySellerDataAll(keyword);
            } else {
                exportData = dataService.searchAllData(keyword);
            }
            
            // 3. 设置响应头
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = "数据导出_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + 
                              java.net.URLEncoder.encode(filename, "UTF-8"));
            
            // 4. 使用POI生成Excel
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("数据列表");
                
                // 创建表头
                Row headerRow = sheet.createRow(0);
                String[] headers = {"ID", "SKU", "Seller", "ISC1", "销售部门", "用户组织"};
                for (int i = 0; i < headers.length; i++) {
                    headerRow.createCell(i).setCellValue(headers[i]);
                }
                
                // 填充数据
                int rowNum = 1;
                for (CollectionData data : exportData) {
                    Row row = sheet.createRow(rowNum++);
                    row.createCell(0).setCellValue(data.getId());
                    row.createCell(1).setCellValue(data.getSku());
                    row.createCell(2).setCellValue(data.getSeller() != null ? data.getSeller() : "");
                    row.createCell(3).setCellValue(data.getIsc1());
                    row.createCell(4).setCellValue(data.getSalesDepart() != null ? data.getSalesDepart() : "");
                    row.createCell(5).setCellValue(data.getUserOrganization() != null ? data.getUserOrganization() : "");
                }
                
                // 自动调整列宽
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }
                
                // 输出到响应流
                workbook.write(response.getOutputStream());
                response.getOutputStream().flush();
                response.getOutputStream().close();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "导出失败: " + e.getMessage());
        }
    }
}
