package com.servlet;

import com.dao.BrandAdDAO;
import com.model.BrandAd;
import com.model.User;
import com.util.DBUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@WebServlet("/UpdateBrandAdServlet")
public class UpdateBrandAdServlet extends HttpServlet {
    private BrandAdDAO brandAdDAO = new BrandAdDAO();

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");
        
        // 声明所有需要的变量
        String currentPage = null;
        String pageSize = null;
        String campaignName = null;
        String uacs = null;
        String idStr = null;
        String sku = null;
        String warehouseSku = null;
        String quantityStr = null;
        String sbsku1 = null;
        String sbquantity1Str = null;
        String sbsku2 = null;
        String sbquantity2Str = null;
        String sbsku3 = null;
        String sbquantity3Str = null;
        String sbsku4 = null;
        String sbquantity4Str = null;
        String sbsku5 = null;
        String sbquantity5Str = null;
        String sellerName = request.getParameter("sellerName");
        // 获取登录用户信息
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");
        
        if (loginUser == null) {
            sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "not_logged_in", 
                                  currentPage, pageSize, campaignName, uacs, null, null, null, null, 
                                  null, null, null, null, null, null, null, null,null,null);
            return;
        }
        
        int loginUserId = loginUser.getId();
        
        try {
            // 获取所有参数
            idStr = request.getParameter("id");
            sku = request.getParameter("sku");
            warehouseSku = request.getParameter("warehouseSku");
            quantityStr = request.getParameter("quantity");
            sbsku1 = request.getParameter("sbsku1");
            sbquantity1Str = request.getParameter("sbquantity1");
            sbsku2 = request.getParameter("sbsku2");
            sbquantity2Str = request.getParameter("sbquantity2");
            sbsku3 = request.getParameter("sbsku3");
            sbquantity3Str = request.getParameter("sbquantity3");
            sbsku4 = request.getParameter("sbsku4");
            sbquantity4Str = request.getParameter("sbquantity4");
            sbsku5 = request.getParameter("sbsku5");
            sbquantity5Str = request.getParameter("sbquantity5");
            currentPage = request.getParameter("currentPage");
            pageSize = request.getParameter("pageSize");
            campaignName = request.getParameter("campaignName");
            uacs = request.getParameter("uacs");
            
            // 参数验证
            if (idStr == null || idStr.trim().isEmpty()) {
                sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_id", 
                                      currentPage, pageSize, campaignName, uacs, null, null, null, null, 
                                      null, null, null, null, null, null, null, null,null,null);
                return;
            }
            
            int id;
            try {
                id = Integer.parseInt(idStr.trim());
            } catch (NumberFormatException e) {
                sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_id_format", 
                                      currentPage, pageSize, campaignName, uacs, null, null, null, null, 
                                      null, null, null, null, null, null, null, null,null,null);
                return;
            }
            
            // 验证SKU是否存在于amazon_data表中
            boolean skuIsValid = isSkuValid(sku);
            
            // 如果SKU不存在，但提供了warehouseSku，也允许更新
            if (!skuIsValid && (warehouseSku == null || warehouseSku.trim().isEmpty())) {
                sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sku", 
                                      currentPage, pageSize, campaignName, uacs, id, 
                                      "SKU无效，请补充仓库SKU信息", sku, warehouseSku, 
                                      sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                      sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                      sbsku5, sbquantity5Str);
                return;
            }
            
            // 验证数量参数
            int quantity = 0;
            if (quantityStr != null && !quantityStr.trim().isEmpty()) {
                try {
                    quantity = Integer.parseInt(quantityStr.trim());
                    if (quantity <= 0) {
                        sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_quantity", 
                                              currentPage, pageSize, campaignName, uacs, id, 
                                              "数量必须大于0", sku, warehouseSku, 
                                              sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                              sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                              sbsku5, sbquantity5Str);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_quantity_format", 
                                          currentPage, pageSize, campaignName, uacs, id, 
                                          "数量格式不正确", sku, warehouseSku, 
                                          sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                          sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                          sbsku5, sbquantity5Str);
                    return;
                }
            }
            
            // 验证SB数量1
            Integer sbquantity1 = null;
            if (sbquantity1Str != null && !sbquantity1Str.trim().isEmpty()) {
                try {
                    sbquantity1 = Integer.parseInt(sbquantity1Str.trim());
                    if (sbquantity1 <= 0) {
                        sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity1", 
                                              currentPage, pageSize, campaignName, uacs, id, 
                                              "SB数量1必须大于0", sku, warehouseSku, 
                                              sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                              sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                              sbsku5, sbquantity5Str);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity1_format", 
                                          currentPage, pageSize, campaignName, uacs, id, 
                                          "SB数量1格式不正确", sku, warehouseSku, 
                                          sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                          sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                          sbsku5, sbquantity5Str);
                    return;
                }
            }
            
            // 验证SB数量2
            Integer sbquantity2 = null;
            if (sbquantity2Str != null && !sbquantity2Str.trim().isEmpty()) {
                try {
                    sbquantity2 = Integer.parseInt(sbquantity2Str.trim());
                    if (sbquantity2 <= 0) {
                        sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity2", 
                                              currentPage, pageSize, campaignName, uacs, id, 
                                              "SB数量2必须大于0", sku, warehouseSku, 
                                              sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                              sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                              sbsku5, sbquantity5Str);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity2_format", 
                                          currentPage, pageSize, campaignName, uacs, id, 
                                          "SB数量2格式不正确", sku, warehouseSku, 
                                          sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                          sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                          sbsku5, sbquantity5Str);
                    return;
                }
            }
            
            // 验证SB数量3
            Integer sbquantity3 = null;
            if (sbquantity3Str != null && !sbquantity3Str.trim().isEmpty()) {
                try {
                    sbquantity3 = Integer.parseInt(sbquantity3Str.trim());
                    if (sbquantity3 <= 0) {
                        sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity3", 
                                              currentPage, pageSize, campaignName, uacs, id, 
                                              "SB数量3必须大于0", sku, warehouseSku, 
                                              sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                              sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                              sbsku5, sbquantity5Str);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity3_format", 
                                          currentPage, pageSize, campaignName, uacs, id, 
                                          "SB数量3格式不正确", sku, warehouseSku, 
                                          sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                          sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                          sbsku5, sbquantity5Str);
                    return;
                }
            }
            
            // 验证SB数量4
            Integer sbquantity4 = null;
            if (sbquantity4Str != null && !sbquantity4Str.trim().isEmpty()) {
                try {
                    sbquantity4 = Integer.parseInt(sbquantity4Str.trim());
                    if (sbquantity4 <= 0) {
                        sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity4", 
                                              currentPage, pageSize, campaignName, uacs, id, 
                                              "SB数量4必须大于0", sku, warehouseSku, 
                                              sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                              sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                              sbsku5, sbquantity5Str);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity4_format", 
                                          currentPage, pageSize, campaignName, uacs, id, 
                                          "SB数量4格式不正确", sku, warehouseSku, 
                                          sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                          sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                          sbsku5, sbquantity5Str);
                    return;
                }
            }
            try {
            	
            	        Integer sbquantity5 = null;
            	// 1. 查询当前记录的 sku 和 uacs（因为用户可能改了）
                BrandAd currentAd = brandAdDAO.findById(id);
                if (currentAd != null) {
                	String platformSku = (sku != null) ? sku.trim() : "";
                    String uacId = currentAd.getUacId();      // 即 uac_id

                    // 2. 构建仓库SKU列表
                    List<Map.Entry<String, Integer>> items = new ArrayList<>();

                    // 主仓库SKU + 数量
                    if (warehouseSku != null && !warehouseSku.trim().isEmpty()) {
                        items.add(new AbstractMap.SimpleEntry<>(warehouseSku.trim(), quantity));
                    }

                    // sbsku1 ~ sbquantity1
                    if (sbsku1 != null && !sbsku1.trim().isEmpty()) {
                        items.add(new AbstractMap.SimpleEntry<>(sbsku1.trim(), sbquantity1));
                    }
                    if (sbsku2 != null && !sbsku2.trim().isEmpty()) {
                        items.add(new AbstractMap.SimpleEntry<>(sbsku2.trim(), sbquantity2));
                    }
                    if (sbsku3 != null && !sbsku3.trim().isEmpty()) {
                        items.add(new AbstractMap.SimpleEntry<>(sbsku3.trim(), sbquantity3));
                    }
                    if (sbsku4 != null && !sbsku4.trim().isEmpty()) {
                        items.add(new AbstractMap.SimpleEntry<>(sbsku4.trim(), sbquantity4));
                    }
                    if (sbsku5 != null && !sbsku5.trim().isEmpty()) {
                        items.add(new AbstractMap.SimpleEntry<>(sbsku5.trim(), sbquantity5));
                    }

                    // 3. 更新映射表
                    boolean mapSuccess = brandAdDAO.updateBrandAdMap(id, platformSku, uacId, items);
                    if (!mapSuccess) {
                        // 可选：记录警告，但不中断主流程
                        System.err.println("Warning: Failed to update brand_ad_map for ad_id=" + id);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // 不影响主更新成功提示
            }
            // 验证SB数量5
            Integer sbquantity5 = null;
            if (sbquantity5Str != null && !sbquantity5Str.trim().isEmpty()) {
                try {
                    sbquantity5 = Integer.parseInt(sbquantity5Str.trim());
                    if (sbquantity5 <= 0) {
                        sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity5", 
                                              currentPage, pageSize, campaignName, uacs, id, 
                                              "SB数量5必须大于0", sku, warehouseSku, 
                                              sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                              sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                              sbsku5, sbquantity5Str);
                        return;
                    }
                } catch (NumberFormatException e) {
                    sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "invalid_sbquantity5_format", 
                                          currentPage, pageSize, campaignName, uacs, id, 
                                          "SB数量5格式不正确", sku, warehouseSku, 
                                          sbsku1, sbquantity1Str, sbsku2, sbquantity2Str, 
                                          sbsku3, sbquantity3Str, sbsku4, sbquantity4Str, 
                                          sbsku5, sbquantity5Str);
                    return;
                }
            }
            
            // 执行更新操作（包含所有字段）
            boolean success = brandAdDAO.updateSkuAndWarehouseSkuAndQuantityAndSbsku1AndSbquantity1AndUserId(
                id, sku, warehouseSku, quantity, sellerName,
                sbsku1, sbquantity1,
                sbsku2, sbquantity2,
                sbsku3, sbquantity3,
                sbsku4, sbquantity4,
                sbsku5, sbquantity5,
                loginUserId);
            
            // 构建重定向URL
            if (success) {
                sendRedirectWithMessage(response, "ListBrandAdServlet", "message", "update_success", 
                                      currentPage, pageSize, campaignName, uacs, null, null, null, null, 
                                      null, null, null, null, null, null, null, null,null,null);
            } else {
                sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "update_failed", 
                                      currentPage, pageSize, campaignName, uacs, null, null, null, null, 
                                      null, null, null, null, null, null, null, null,null,null);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "database_error", 
                                  currentPage, pageSize, campaignName, uacs, null, null, null, null, 
                                  null, null, null, null, null, null, null, null,null,null);
        } catch (Exception e) {
            e.printStackTrace();
            sendRedirectWithMessage(response, "ListBrandAdServlet", "error", "system_error", 
                                  currentPage, pageSize, campaignName, uacs, null, null, null, null, 
                                  null, null, null, null, null, null, null, null,null,null);
        }
    }

    // 修改重定向方法，添加所有新字段参数
    private void sendRedirectWithMessage(HttpServletResponse response, String url, 
                                        String param, String value,
                                        String currentPage, String pageSize, 
                                        String campaignName, String uacs,
                                        Integer id, String errorMsg, 
                                        String inputSku, String warehouseSku,
                                        String sbsku1, String sbquantity1Str,
                                        String sbsku2, String sbquantity2Str,
                                        String sbsku3, String sbquantity3Str,
                                        String sbsku4, String sbquantity4Str,
                                        String sbsku5, String sbquantity5Str) throws IOException {
        StringBuilder redirectUrl = new StringBuilder(url + "?");
        
        // 添加分页和搜索参数
        if (currentPage != null && !currentPage.isEmpty()) {
            redirectUrl.append("page=").append(currentPage).append("&");
        }
        if (pageSize != null && !pageSize.isEmpty()) {
            redirectUrl.append("size=").append(pageSize).append("&");
        }
        if (campaignName != null && !campaignName.isEmpty()) {
            redirectUrl.append("campaignName=").append(encodeParam(campaignName)).append("&");
        }
        if (uacs != null && !uacs.isEmpty()) {
            redirectUrl.append("uacs=").append(encodeParam(uacs)).append("&");
        }
        
        // 添加错误参数
        redirectUrl.append(param).append("=").append(value);
        
        // 添加记录ID
        if (id != null) {
            redirectUrl.append("&id=").append(id);
        }
        
        // 添加错误消息
        if (errorMsg != null) {
            redirectUrl.append("&errorMsg=").append(URLEncoder.encode(errorMsg, StandardCharsets.UTF_8.name()));
        }
        
        // 添加用户输入的SKU
        if (inputSku != null) {
            redirectUrl.append("&inputSku=").append(URLEncoder.encode(inputSku, StandardCharsets.UTF_8.name()));
        }
        
        // 添加仓库SKU（如果存在）
        if (warehouseSku != null && !warehouseSku.trim().isEmpty()) {
            redirectUrl.append("&warehouseSku=").append(URLEncoder.encode(warehouseSku, StandardCharsets.UTF_8.name()));
        }
        
        // 添加所有新字段
        if (sbsku1 != null && !sbsku1.trim().isEmpty()) {
            redirectUrl.append("&sbsku1=").append(URLEncoder.encode(sbsku1, StandardCharsets.UTF_8.name()));
        }
        if (sbquantity1Str != null && !sbquantity1Str.trim().isEmpty()) {
            redirectUrl.append("&sbquantity1=").append(URLEncoder.encode(sbquantity1Str, StandardCharsets.UTF_8.name()));
        }
        if (sbsku2 != null && !sbsku2.trim().isEmpty()) {
            redirectUrl.append("&sbsku2=").append(URLEncoder.encode(sbsku2, StandardCharsets.UTF_8.name()));
        }
        if (sbquantity2Str != null && !sbquantity2Str.trim().isEmpty()) {
            redirectUrl.append("&sbquantity2=").append(URLEncoder.encode(sbquantity2Str, StandardCharsets.UTF_8.name()));
        }
        if (sbsku3 != null && !sbsku3.trim().isEmpty()) {
            redirectUrl.append("&sbsku3=").append(URLEncoder.encode(sbsku3, StandardCharsets.UTF_8.name()));
        }
        if (sbquantity3Str != null && !sbquantity3Str.trim().isEmpty()) {
            redirectUrl.append("&sbquantity3=").append(URLEncoder.encode(sbquantity3Str, StandardCharsets.UTF_8.name()));
        }
        if (sbsku4 != null && !sbsku4.trim().isEmpty()) {
            redirectUrl.append("&sbsku4=").append(URLEncoder.encode(sbsku4, StandardCharsets.UTF_8.name()));
        }
        if (sbquantity4Str != null && !sbquantity4Str.trim().isEmpty()) {
            redirectUrl.append("&sbquantity4=").append(URLEncoder.encode(sbquantity4Str, StandardCharsets.UTF_8.name()));
        }
        if (sbsku5 != null && !sbsku5.trim().isEmpty()) {
            redirectUrl.append("&sbsku5=").append(URLEncoder.encode(sbsku5, StandardCharsets.UTF_8.name()));
        }
        if (sbquantity5Str != null && !sbquantity5Str.trim().isEmpty()) {
            redirectUrl.append("&sbquantity5=").append(URLEncoder.encode(sbquantity5Str, StandardCharsets.UTF_8.name()));
        }
        
        response.sendRedirect(redirectUrl.toString());
    }

    // 原始重定向方法（无id和errorMsg）
    private void sendRedirectWithMessage(HttpServletResponse response, String url, 
                                        String param, String value,
                                        String currentPage, String pageSize, 
                                        String campaignName, String uacs) throws IOException {
        sendRedirectWithMessage(response, url, param, value, 
                              currentPage, pageSize, campaignName, uacs, 
                              null, null, null, null, 
                              null, null, null, null, null, null, null, null, null, null);
    }

    // URL编码工具方法
    private String encodeParam(String param) {
        try {
            return URLEncoder.encode(param, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            return param;
        }
    }
    
    // 检查SKU是否存在于amazon_data表
 // 新增方法：检查 SKU 是否有效（在 amazon_data 或 brand_ad_map 中）
    private boolean isSkuValid(String sku) throws SQLException {
        if (sku == null || sku.trim().isEmpty()) {
            return false;
        }
        String trimmedSku = sku.trim();
        
        Connection conn = null;
        PreparedStatement pstmt1 = null, pstmt2 = null;
        ResultSet rs1 = null, rs2 = null;
        
        try {
            conn = DBUtil.getConnection();
            
            // 1. 检查 amazon_data
            String sql1 = "SELECT 1 FROM amazon_data WHERE sku = ? LIMIT 1";
            pstmt1 = conn.prepareStatement(sql1);
            pstmt1.setString(1, trimmedSku);
            rs1 = pstmt1.executeQuery();
            if (rs1.next()) {
                return true;
            }
            
            // 2. 检查 brand_ad_map
            String sql2 = "SELECT 1 FROM brand_ad_map WHERE platform_sku = ? LIMIT 1";
            pstmt2 = conn.prepareStatement(sql2);
            pstmt2.setString(1, trimmedSku);
            rs2 = pstmt2.executeQuery();
            return rs2.next();
            
        } finally {
            try { if (rs1 != null) rs1.close(); } catch (SQLException e) { /* ignore */ }
            try { if (pstmt1 != null) pstmt1.close(); } catch (SQLException e) { /* ignore */ }
            try { if (rs2 != null) rs2.close(); } catch (SQLException e) { /* ignore */ }
            try { if (pstmt2 != null) pstmt2.close(); } catch (SQLException e) { /* ignore */ }
            DBUtil.closeConnection(conn);
        }
    }

    
    // 支持GET请求
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doPost(request, response);
    }
}
