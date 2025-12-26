<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.model.BrandAd, java.util.List" %>
<%!
    // 部门代码转换为部门名称
    public String getDepartmentName(String deptCode) {
        if (deptCode == null || deptCode.trim().isEmpty()) {
            return "";
        }
        
        switch (deptCode.trim()) {
        case "0": // 新增：sales_depart=0 → 暂无部门
            return "未知部门";
            case "1":
                return "销售一部";
            case "2":
                return "销售二部";
            case "3":
                return "乐器项目部";
            case "4":
                return "大件项目部";
            default:
                return deptCode;
        }
    }
%>
<%
    // 获取分页参数
    String pageStr = request.getParameter("page");
    String sizeStr = request.getParameter("size");
    int currentPage = (pageStr != null && !pageStr.isEmpty()) ? Integer.parseInt(pageStr) : 1;
    int pageSize = (sizeStr != null && !sizeStr.isEmpty()) ? Integer.parseInt(sizeStr) : 10;

    // 获取总记录数
    Integer total = (Integer) request.getAttribute("total");
    if (total == null) total = 0;
    int totalPages = (int) Math.ceil((double) total / pageSize);

    // 构建基础URL（保留搜索参数）
    StringBuilder baseUrl = new StringBuilder("ListBrandAdServlet?");
    String campaignName = request.getParameter("campaignName");
    String skuParam = request.getParameter("sku");
    String uacsParam = request.getParameter("uacs");

    if (campaignName != null && !campaignName.trim().isEmpty()) {
        baseUrl.append("campaignName=").append(java.net.URLEncoder.encode(campaignName, "UTF-8")).append("&");
    }
    if (skuParam != null && !skuParam.trim().isEmpty()) {
        baseUrl.append("sku=").append(java.net.URLEncoder.encode(skuParam, "UTF-8")).append("&");
    }
    if (uacsParam != null && !uacsParam.trim().isEmpty()) {
        baseUrl.append("uacs=").append(java.net.URLEncoder.encode(uacsParam, "UTF-8")).append("&");
    }
    String baseUrlStr = baseUrl.toString();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>品牌广告数据表</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
    .header { 
    position: relative; /* 改为相对定位，作为返回链接的定位参考 */
    margin-bottom: 20px; 
    padding-top: 40px; /* 顶部留空间给返回首页链接 */
}
h1 { 
    color: #333; 
    margin: 0; 
    padding-bottom: 10px;
    border-bottom: 2px solid #409eff; /* 可选：和之前汇率页面标题样式统一，更美观 */
}
.back-link { 
    position: absolute; /* 绝对定位到header左上方 */
    top: 0; /* 贴header顶部 */
    left: 0; /* 贴header左侧 */
    color: #409eff; 
    text-decoration: underline; 
    font-weight: bold;
    font-size: 16px; /* 可选：调整字号，和汇率页面保持一致 */
    cursor: pointer;
}
.back-link:hover {
    color: #3390e0;
    text-decoration: none;
}

        /* 搜索区域 */
        .search-box {
            background: #f8f9fa;
            padding: 15px;
            border-radius: 6px;
            margin-bottom: 20px;
        }
        .search-form {
            display: flex;
            gap: 15px;
            flex-wrap: wrap;
            align-items: end;
        }
        .search-form div {
            display: flex;
            flex-direction: column;
        }
        .search-form label {
            font-size: 12px;
            color: #666;
            margin-bottom: 4px;
        }
        .search-form input {
            padding: 8px 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            width: 180px;
        }
        .search-btn {
            padding: 8px 16px;
            background: #409eff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .search-btn:hover {
            background: #3390e0;
        }

        /* 表格 */
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f9fa; color: #333; }
        tr:hover { background-color: #f9f9f9; }
        .no-data { text-align: center; color: #999; padding: 30px; font-size: 16px; }

        /* 分页样式 */
        .pagination {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 20px;
            padding-top: 15px;
            border-top: 1px solid #eee;
            flex-wrap: wrap;
            gap: 15px;
        }
        .page-info { color: #666; }
        .page-nav, .page-size-selector, .page-input {
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .page-size-selector select,
        .page-input input {
            padding: 6px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        .page-input button {
            padding: 6px 12px;
            background: #409eff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }

        /* 页码链接样式 */
        .page-link {
            display: inline-block;
            padding: 6px 10px;
            margin: 0 2px;
            text-decoration: none;
            color: #409eff;
            border: 1px solid #ddd;
            border-radius: 4px;
            background: white;
            transition: all 0.2s;
        }
        .page-link:hover {
            background: #f0f9ff;
        }
        .page-link.current {
            background: #409eff;
            color: white;
            border-color: #409eff;
        }
        .page-link.disabled {
            color: #ccc;
            border-color: #eee;
            cursor: not-allowed;
        }

        /* 修改按钮样式 */
        .edit-btn {
            padding: 4px 8px;
            background: #409eff;
            color: white;
            border: none;
            border-radius: 3px;
            cursor: pointer;
            font-size: 12px;
        }
        .edit-btn:hover {
            background: #3390e0;
        }

        /* 模态框样式 */
        .modal {
            display: none;
            position: fixed;
            z-index: 1000;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0,0,0,0.5);
        }
        .modal-content {
            background-color: white;
            margin: 10% auto;
            padding: 20px;
            border-radius: 8px;
            width: 400px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.3);
            /* ✅ 核心修复：限制高度 + 启用滚动 */
            max-height: 80vh;
            overflow-y: auto;
            display: flex;
            flex-direction: column;
        }
        .modal-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }
        .modal-header h2 {
            margin: 0;
            font-size: 18px;
            color: #333;
        }
        .close {
            color: #aaa;
            font-size: 24px;
            font-weight: bold;
            cursor: pointer;
        }
        .close:hover {
            color: #000;
        }

        /* 可滚动表单区域 */
        #scrollable-form {
            flex: 1;
            overflow-y: auto;
            padding: 0 10px;
            margin-bottom: 15px;
        }

        .form-group {
            margin-bottom: 15px;
        }
        .form-group label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #555;
            font-size: 14px;
        }
        .form-group input[type="text"] {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            box-sizing: border-box;
        }
        .form-group input[readonly] {
            background-color: #f5f5f5;
            color: #666;
        }
        .modal-footer {
            text-align: right;
            margin-top: 20px;
            padding-top: 15px;
            border-top: 1px solid #eee;
            display: flex;
            gap: 10px;
        }
        .btn-primary {
            padding: 8px 16px;
            background: #409eff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .btn-primary:hover {
            background: #3390e0;
        }
        .btn-secondary {
            padding: 8px 16px;
            background: #6c757d;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .btn-secondary:hover {
            background: #5a6268;
        }

        /* 仓库SKU收集区域样式 */
        #warehouseSkuSection {
            display: none;
            margin-top: 15px;
            padding-top: 15px;
            border-top: 1px solid #eee;
        }
        #warehouseSkuSection h3 {
            margin: 0 0 10px;
            color: #d32f2f;
        }

        /* 仓库SKU与数量并排布局 */
        .sku-quantity-row {
            display: flex;
            gap: 15px;
            align-items: flex-start;
        }

        .sku-input-wrapper,
        .quantity-input-wrapper {
            flex: 1;
        }

        /* 仓库SKU占2/3，数量占1/3 */
        .sku-input-wrapper {
            flex: 2;
        }

        .quantity-input-wrapper {
            flex: 1;
        }

        /* 确保输入框宽度100% + 高度统一 */
        .sku-input-wrapper input,
        .quantity-input-wrapper input {
            width: 100%;
            box-sizing: border-box;
            height: 40px;
            padding: 0 12px;
            font-size: 14px;
            line-height: 40px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }

        /* 输入框聚焦样式 */
        .sku-input-wrapper input:focus,
        .quantity-input-wrapper input:focus {
            outline: none;
            border-color: #2f54eb;
            box-shadow: 0 0 0 2px rgba(47, 84, 235, 0.1);
        }

        /* 小屏幕下堆叠显示 */
        @media (max-width: 600px) {
            .sku-quantity-row {
                flex-direction: column;
                gap: 12px;
            }
            
            .sku-input-wrapper,
            .quantity-input-wrapper {
                flex: none;
            }
            
            .modal-content {
                width: 95%;
                margin: 5% auto;
                max-height: 90vh;
            }
        }

        /* 成功提示框样式 */
        .success-message {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background-color: #d4edda;
            color: #155724;
            padding: 20px 30px;
            border: 1px solid #c3e6cb;
            border-radius: 6px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.2);
            z-index: 2000;
            opacity: 0;
            transition: opacity 0.3s ease-in-out;
            font-size: 16px;
            font-weight: bold;
        }
        .success-message.show {
            opacity: 1;
        }
        
        /* 错误提示框样式 */
        .error-message {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background-color: #f8d7da;
            color: #721c24;
            padding: 20px 30px;
            border: 1px solid #f5c6cb;
            border-radius: 6px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.2);
            z-index: 2000;
            opacity: 0;
            transition: opacity 0.3s ease-in-out;
            font-size: 16px;
            font-weight: bold;
        }
        .error-message.show {
            opacity: 1;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>品牌广告数据表</h1>
            <a href="index.jsp" class="back-link">← 返回首页</a>
        </div>

        <!-- 搜索框 -->
        <div class="search-box">
            <form action="ListBrandAdServlet" method="get" class="search-form">
                <input type="hidden" name="page" value="1">
                <input type="hidden" name="size" value="<%= pageSize %>">

                <div>
                    <label>广告名称</label>
                    <input type="text" name="campaignName" 
                           value="<%= request.getParameter("campaignName") != null ? request.getParameter("campaignName") : "" %>">
                </div>
                <div>
                    <label>UACS</label>
                    <input type="text" name="uacs" 
                           value="<%= request.getParameter("uacs") != null ? request.getParameter("uacs") : "" %>">
                </div>
                <button type="submit" class="search-btn">搜索</button>
                <button type="button" class="search-btn" 
                        onclick="location.href='ListBrandAdServlet?page=1&size=<%= pageSize %>'">↺ 重置</button>
            </form>
        </div>

        <%
            List<BrandAd> brandAds = (List<BrandAd>) request.getAttribute("brandAds");
            if (brandAds == null || brandAds.isEmpty()) {
        %>
            <div class="no-data">暂无品牌广告数据</div>
        <%
            } else {
        %>
            <table>
                <thead>
                    <tr>
                        <th>序号</th>
                        <th>广告名称</th>
                        <th>UACS</th>
                        <th>平台SKU</th>
                        <th>所属部门</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <% 
                        int startIndex = (currentPage - 1) * pageSize;
                        for (int i = 0; i < brandAds.size(); i++) {
                            BrandAd ad = brandAds.get(i);
                            int displayIndex = startIndex + i + 1;
                            String salesDepart = ad.getSalesDepart();
                            String departName = getDepartmentName(salesDepart);
                    %>
                    <tr>
                        <td><%= displayIndex %></td>
                        <td><%= ad.getCampaignName() != null ? ad.getCampaignName() : "" %></td>
                        <td><%= ad.getUacs() != null ? ad.getUacs() : "" %></td>
                        <td><%= ad.getSku() != null ? ad.getSku() : "" %></td>
                        <td><%= departName %></td>
                        <td>
                            <button class="edit-btn" onclick="openEditModal(
                                '<%= displayIndex %>',
                                '<%= ad.getId() %>',
                                '<%= ad.getCampaignName() != null ? ad.getCampaignName() : "" %>',
                                '<%= ad.getUacs() != null ? ad.getUacs() : "" %>',
                                '<%= ad.getSku() != null ? ad.getSku() : "" %>',
                                '<%= departName %>',
                                '<%= ad.getWarehouseSku() != null ? ad.getWarehouseSku() : "" %>',
                                '<%= ad.getSbsku1() != null ? ad.getSbsku1() : "" %>',
                                '<%= ad.getSbquantity1() != null ? ad.getSbquantity1() : "" %>'
                            )">修改</button>
                        </td>
                    </tr>
                    <% } %>
                </tbody>
            </table>

            <!-- 分页控件 -->
            <div class="pagination">
                <div class="page-info">
                    共 <%= total %> 条数据，当前第 <%= currentPage %> 页，共 <%= totalPages %> 页
                </div>

                <div class="page-nav">
                    <% if (currentPage > 1) { %>
                        <a href="<%= baseUrlStr %>page=<%= currentPage - 1 %>&size=<%= pageSize %>" class="page-link">&laquo; 上一页</a>
                    <% } else { %>
                        <span class="page-link disabled">&laquo; 上一页</span>
                    <% } %>

                    <%
                        if (totalPages > 0) {
                            int startPage = Math.max(1, currentPage - 2);
                            int endPage = Math.min(totalPages, currentPage + 2);

                            if (endPage - startPage < 4) {
                                if (startPage == 1) {
                                    endPage = Math.min(totalPages, 5);
                                } else if (endPage == totalPages) {
                                    startPage = Math.max(1, totalPages - 4);
                                }
                            }

                            for (int i = startPage; i <= endPage; i++) {
                                if (i == currentPage) {
                    %>
                        <span class="page-link current"><%= i %></span>
                    <%
                                } else {
                    %>
                        <a href="<%= baseUrlStr %>page=<%= i %>&size=<%= pageSize %>" class="page-link"><%= i %></a>
                    <%
                                }
                            }
                        }
                    %>

                    <% if (currentPage < totalPages) { %>
                        <a href="<%= baseUrlStr %>page=<%= currentPage + 1 %>&size=<%= pageSize %>" class="page-link">下一页 &raquo;</a>
                    <% } else { %>
                        <span class="page-link disabled">下一页 &raquo;</span>
                    <% } %>
                </div>

                <div style="display: flex; gap: 20px; align-items: center;">
                    <div class="page-size-selector">
                        每页显示：
                        <select onchange="changePageSize(this.value)">
                            <option value="10" <%= pageSize == 10 ? "selected" : "" %>>10</option>
                            <option value="20" <%= pageSize == 20 ? "selected" : "" %>>20</option>
                            <option value="50" <%= pageSize == 50 ? "selected" : "" %>>50</option>
                            <option value="100" <%= pageSize == 100 ? "selected" : "" %>>100</option>
                        </select>
                    </div>

                    <div class="page-input">
                        跳转到第
                        <input type="number" id="pageInput" min="1" max="<%= totalPages %>" value="<%= currentPage %>" style="width: 60px;">
                        页
                        <button onclick="goToPage()">跳转</button>
                    </div>
                </div>
            </div>
        <%
            }
        %>
    </div>

    <!-- 编辑模态框 -->
    <div id="editModal" class="modal">
        <div class="modal-content">
            <div class="modal-header">
                <h2>编辑品牌广告信息</h2>
                <span class="close" onclick="closeEditModal()">&times;</span>
            </div>

            <!-- ✅ 可滚动内容区域 -->
            <div id="scrollable-form">
                <form id="editForm" action="<%= request.getContextPath() %>/UpdateBrandAdServlet" method="post">
                    <input type="hidden" id="editId" name="id">
                    <input type="hidden" name="currentPage" value="<%= currentPage %>">
                    <input type="hidden" name="pageSize" value="<%= pageSize %>">
                    <input type="hidden" name="campaignName" value="<%= request.getParameter("campaignName") != null ? request.getParameter("campaignName") : "" %>">
                    <input type="hidden" name="uacs" value="<%= request.getParameter("uacs") != null ? request.getParameter("uacs") : "" %>">
                    
                    <div class="form-group">
                        <label>序号:</label>
                        <input type="text" id="editDisplayIndex" readonly>
                    </div>
                    
                    <div class="form-group">
                        <label>广告名称:</label>
                        <input type="text" id="editCampaignName" name="campaignName" readonly>
                    </div>
                    
                    <div class="form-group">
                        <label>UACS:</label>
                        <input type="text" id="editUacs" name="uacs" readonly>
                    </div>
                    
                    <div class="form-group">
                        <label>所属部门:</label>
                        <input type="text" id="editSalesDepart" readonly>
                    </div>
                    
                    <div class="form-group">
                        <label>平台SKU:<span style="color:red;">*</span></label>
                        <input type="text" id="editSku" name="sku" 
                               oninput="validateSku(this)" 
                               onblur="validateSku(this)" 
                               class="form-control">
                        <div id="skuError" style="color:red; font-size:12px; margin-top:4px; display:none;">
                            平台SKU不能为空
                        </div>
                        <div id="serverError" style="color:red; font-size:12px; margin-top:4px; display:none;"></div>
                    </div>
                    <div id="sellerNameSection" style="display: none; margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee;">
    <div class="form-group">
        <label>seller:<span style="color:red;">*</span></label>
        <input type="text" id="sellerName" name="sellerName" class="form-control" 
               oninput="validateSellerName(this)" 
               placeholder="请输入该平台sku对应的seller">
        <div id="sellerNameError" style="color:red; font-size:12px; margin-top:4px; display:none;">
            seller不能为空
        </div>
        <div id="sellerNameServerError" style="color:red; font-size:12px; margin-top:4px; display:none;">
            seller不存在于数据库中
        </div>
    </div>
</div>
                    <!-- 仓库SKU收集区域 -->
                    <div id="warehouseSkuSection" style="display: none; margin-top: 15px; padding-top: 15px; border-top: 1px solid #eee;">
                        <h3>请补充对应的仓库SKU信息（当前平台SKU: <span id="currentSku"></span>）</h3>
                        
                        <!-- 第一组：原仓库SKU和数量 -->
                        <div class="form-group sku-quantity-row">
                            <div class="sku-input-wrapper">
                                <label>仓库SKU:<span style="color:red;">*</span></label>
                                <input type="text" id="warehouseSku" name="warehouseSku" class="form-control" 
                                       oninput="validateWarehouseSku(this)" 
                                       placeholder="请输入该平台SKU对应的仓库SKU">
                                <div id="warehouseSkuError" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                    仓库SKU不能为空
                                </div>
                                    <div id="warehouseSkuServerError" style="color:red; font-size:12px; margin-top:4px; display:none;">
        仓库SKU不存在于数据库中
    </div>
                            </div>
                            
                            <div class="quantity-input-wrapper">
                                <label>数量:<span style="color:red;">*</span></label>
                                <input type="number" id="quantity" name="quantity" min="1" class="form-control" 
                                       oninput="validateQuantity(this)" 
                                       placeholder="数量">
                                <div id="quantityError" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                    数量必须大于0
                                </div>
                            </div>
                        </div>
                        
                        <!-- 第二组：SBSKU1和SB数量1 -->
                        <div class="form-group sku-quantity-row">
                            <div class="sku-input-wrapper">
                                <label>仓库sku:<span style="color:red;">*</span></label>
                                <input type="text" id="sbsku1" name="sbsku1" class="form-control" 
                                       oninput="validateSbsku1(this)" 
                                       placeholder="如果只对应1个仓库SKU就填一个空">
                                <div id="sbsku1Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                    SKU不能为空
                                </div>
                                    <div id="sbsku1ServerError" style="color:red; font-size:12px; margin-top:4px; display:none;">
        仓库SKU不存在于数据库中
    </div>
                            </div>
                            
                            <div class="quantity-input-wrapper">
                                <label>数量:<span style="color:red;">*</span></label>
                                <input type="number" id="sbquantity1" name="sbquantity1" min="1" class="form-control" 
                                       oninput="validateSbquantity1(this)" 
                                       placeholder="数量">
                                <div id="sbquantity1Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                    数量必须大于0
                                </div>
                            </div>
                        </div>
                        
                        <!-- 新增的4个SKU数量对，初始折叠 -->
                        <div id="additionalSkuSection" style="display: none; margin-top: 15px;">
                            <div class="form-group sku-quantity-row">
                                <div class="sku-input-wrapper">
                                    <label>仓库sku:<span style="color:red;">*</span></label>
                                    <input type="text" id="sbsku2" name="sbsku2" class="form-control" 
                                           oninput="validateSbsku2(this)" 
                                           placeholder="请输入仓库SKU">
                                    <div id="sbsku2Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                        SKU不能为空
                                    </div>
                                        <div id="sbsku2ServerError" style="color:red; font-size:12px; margin-top:4px; display:none;">
        仓库SKU不存在于数据库中
    </div>
                                </div>
                                
                                <div class="quantity-input-wrapper">
                                    <label>数量:<span style="color:red;">*</span></label>
                                    <input type="number" id="sbquantity2" name="sbquantity2" min="1" class="form-control" 
                                           oninput="validateSbquantity2(this)" 
                                           placeholder="数量">
                                    <div id="sbquantity2Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                        数量必须大于0
                                    </div>
                                </div>
                            </div>
                            
                            <div class="form-group sku-quantity-row">
                                <div class="sku-input-wrapper">
                                    <label>仓库sku:<span style="color:red;">*</span></label>
                                    <input type="text" id="sbsku3" name="sbsku3" class="form-control" 
                                           oninput="validateSbsku3(this)" 
                                           placeholder="请输入仓库SKU">
                                    <div id="sbsku3Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                        SKU不能为空
                                    </div>
                                        <div id="sbsku3ServerError" style="color:red; font-size:12px; margin-top:4px; display:none;">
        仓库SKU不存在于数据库中
    </div>
                                </div>
                                
                                <div class="quantity-input-wrapper">
                                    <label>数量:<span style="color:red;">*</span></label>
                                    <input type="number" id="sbquantity3" name="sbquantity3" min="1" class="form-control" 
                                           oninput="validateSbquantity3(this)" 
                                           placeholder="数量">
                                    <div id="sbquantity3Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                        数量必须大于0
                                    </div>
                                </div>
                            </div>
                            
                            <div class="form-group sku-quantity-row">
                                <div class="sku-input-wrapper">
                                    <label>仓库sku:<span style="color:red;">*</span></label>
                                    <input type="text" id="sbsku4" name="sbsku4" class="form-control" 
                                           oninput="validateSbsku4(this)" 
                                           placeholder="请输入仓库SKU">
                                    <div id="sbsku4Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                        SKU不能为空
                                    </div>
                                        <div id="sbsku4ServerError" style="color:red; font-size:12px; margin-top:4px; display:none;">
        仓库SKU不存在于数据库中
    </div>
                                </div>
                                
                                <div class="quantity-input-wrapper">
                                    <label>数量:<span style="color:red;">*</span></label>
                                    <input type="number" id="sbquantity4" name="sbquantity4" min="1" class="form-control" 
                                           oninput="validateSbquantity4(this)" 
                                           placeholder="数量">
                                    <div id="sbquantity4Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                        数量必须大于0
                                    </div>
                                </div>
                            </div>
                            
                            <div class="form-group sku-quantity-row">
                                <div class="sku-input-wrapper">
                                    <label>仓库sku:<span style="color:red;">*</span></label>
                                    <input type="text" id="sbsku5" name="sbsku5" class="form-control" 
                                           oninput="validateSbsku5(this)" 
                                           placeholder="请输入仓库SKU">
                                    <div id="sbsku5Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                        SKU不能为空
                                    </div>
                                        <div id="sbsku5ServerError" style="color:red; font-size:12px; margin-top:4px; display:none;">
        仓库SKU不存在于数据库中
    </div>
                                </div>
                                
                                <div class="quantity-input-wrapper">
                                    <label>数量:<span style="color:red;">*</span></label>
                                    <input type="number" id="sbquantity5" name="sbquantity5" min="1" class="form-control" 
                                           oninput="validateSbquantity5(this)" 
                                           placeholder="数量">
                                    <div id="sbquantity5Error" style="color:red; font-size:12px; margin-top:4px; display:none;">
                                        数量必须大于0
                                    </div>
                                </div>
                            </div>
                        </div>
                        
                        <!-- 折叠/展开按钮 -->
                        <div style="text-align: center; margin-top: 10px;">
                            <button type="button" class="btn-secondary" onclick="toggleAdditionalSkuSection()">
                                <span id="toggleText">展开更多SKU</span>
                            </button>
                        </div>
                    </div>
                    
                    <div class="modal-footer">
                        <button type="button" class="btn-secondary" onclick="closeEditModal()">取消</button>
                        <button type="submit" class="btn-primary" form="editForm">保存</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- 成功提示框 -->
    <div id="successMessage" class="success-message">修改成功！</div>
    
    <!-- 错误提示框 -->
    <div id="errorMessage" class="error-message">SKU验证失败！</div>

<script>
    function changePageSize(size) {
        const url = new URL(window.location);
        url.searchParams.set('size', size);
        url.searchParams.set('page', '1');
        window.location.href = url.toString();
    }

    function goToPage() {
        const page = document.getElementById('pageInput').value;
        const maxPage = <%= totalPages %>;
        if (page < 1) {
            alert('页码不能小于1');
            return;
        }
        if (page > maxPage && maxPage > 0) {
            alert('页码不能大于' + maxPage);
            return;
        }
        const url = new URL(window.location);
        url.searchParams.set('page', page);
        window.location.href = url.toString();
    }

    function openEditModal(displayIndex, id, campaignName, uacs, sku, salesDepart, warehouseSku, sbsku1, sbquantity1) {
        const urlParams = new URLSearchParams(window.location.search);
        const inputSku = urlParams.get('inputSku');
        
        if (inputSku !== null && inputSku.trim() !== '') {
            sku = inputSku;
        }
        const savedQuantity = urlParams.get('quantity');
        if (savedQuantity) {
            document.getElementById('quantity').value = savedQuantity;
        } else {
            document.getElementById('quantity').value = '';
        }

        function escapeHtml(text) {
            if (!text) return '';
            const map = {
                '&': '&amp;',
                '<': '&lt;',
                '>': '&gt;',
                '"': '&quot;',
                "'": '&#039;'
            };
            return text.replace(/[&<>"']/g, function(m) { return map[m]; });
        }

        document.getElementById('editDisplayIndex').value = escapeHtml(displayIndex);
        document.getElementById('editId').value = escapeHtml(id);
        document.getElementById('editCampaignName').value = escapeHtml(campaignName);
        document.getElementById('editUacs').value = escapeHtml(uacs);
        document.getElementById('editSku').value = escapeHtml(sku);
        document.getElementById('editSalesDepart').value = escapeHtml(salesDepart);
        
        if (warehouseSku && warehouseSku.trim() !== '') {
            document.getElementById('warehouseSku').value = escapeHtml(warehouseSku);
        } else {
            document.getElementById('warehouseSku').value = '';
        }
        
        document.getElementById('sbsku1').value = escapeHtml(sbsku1);
        document.getElementById('sbquantity1').value = escapeHtml(sbquantity1);

        document.getElementById('skuError').style.display = 'none';
        document.getElementById('serverError').style.display = 'none';
        document.getElementById('editSku').classList.remove('is-invalid');
        document.getElementById('warehouseSkuSection').style.display = 'none';

        if (inputSku !== null && inputSku.trim() !== '') {
            const serverErrorDiv = document.getElementById('serverError');
            serverErrorDiv.textContent = '该SKU不存在于亚马逊商品库中，请补充映射关系';
            serverErrorDiv.style.display = 'block';
            document.getElementById('warehouseSkuSection').style.display = 'block';
            document.getElementById('currentSku').textContent = inputSku;
            
            const savedWarehouseSku = urlParams.get('warehouseSku');
            if (savedWarehouseSku) {
                document.getElementById('warehouseSku').value = decodeURIComponent(savedWarehouseSku);
            }
            
            const savedSbsku1 = urlParams.get('sbsku1');
            if (savedSbsku1) {
                document.getElementById('sbsku1').value = decodeURIComponent(savedSbsku1);
            }
            
            const savedSbquantity1 = urlParams.get('sbquantity1');
            if (savedSbquantity1) {
                document.getElementById('sbquantity1').value = decodeURIComponent(savedSbquantity1);
            }
            
            setTimeout(() => {
                const skuInput = document.getElementById('editSku');
                checkSkuExists(skuInput);
            }, 100);
        } else {
            setTimeout(() => {
                const skuInput = document.getElementById('editSku');
                checkSkuExists(skuInput);
            }, 100);
        }

        document.getElementById('editModal').style.display = 'block';
    }

    function closeEditModal() {
        document.getElementById('editModal').style.display = 'none';
    }
    
    function showSuccessMessage() {
        const successMsg = document.getElementById('successMessage');
        successMsg.classList.add('show');
        setTimeout(() => successMsg.classList.remove('show'), 2000);
    }
    
    function showErrorMessage(message) {
        const errorMsg = document.getElementById('errorMessage');
        errorMsg.textContent = message || 'SKU验证失败！';
        errorMsg.classList.add('show');
        setTimeout(() => errorMsg.classList.remove('show'), 3000);
    }

    window.addEventListener('load', function() {
        const urlParams = new URLSearchParams(window.location.search);
        const error = urlParams.get('error');
        const errorMsg = urlParams.get('errorMsg');
        const idParam = urlParams.get('id');
        const inputSku = urlParams.get('inputSku'); 
        const warehouseSku = urlParams.get('warehouseSku');
        const sbsku1 = urlParams.get('sbsku1');
        const sbquantity1 = urlParams.get('sbquantity1');

        if (urlParams.get('message') === 'update_success') {
            showSuccessMessage();
            const savedQuantity = urlParams.get('quantity');
            if (savedQuantity) {
                document.getElementById('quantity').value = savedQuantity;
            }
            const url = new URL(window.location);
            url.searchParams.delete('message');
            history.replaceState({}, document.title, url.toString());
        }
        
        if (error === 'invalid_sku' && idParam) {
            const rows = document.querySelectorAll('table tbody tr');
            let found = false;
            
            rows.forEach(row => {
                const button = row.querySelector('.edit-btn');
                if (button && button.onclick) {
                    const onclickStr = button.onclick.toString();
                    const regex = /openEditModal\(\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*,\s*'([^']*)'\s*\)/;
                    const match = onclickStr.match(regex);
                    
                    if (match && match[2] === idParam) {
                        openEditModal(
                            match[1], 
                            match[2], 
                            match[3], 
                            match[4], 
                            match[5], 
                            match[6],
                            match[7],
                            match[8],
                            match[9]
                        );
                        found = true;
                    }
                }
            });
            
            if (!found) {
                showErrorMessage(errorMsg || 'SKU验证失败：无效的SKU');
            }
            
            const url = new URL(window.location);
            url.searchParams.delete('error');
            url.searchParams.delete('errorMsg');
            url.searchParams.delete('id');
            url.searchParams.delete('inputSku');
            url.searchParams.delete('warehouseSku');
            url.searchParams.delete('sbsku1');
            url.searchParams.delete('sbquantity1');
            history.replaceState({}, document.title, url.toString());
        }
    });
    
    function validateSku(input) {
        const errorDiv = document.getElementById('skuError');
        if (input.value.trim() === '') {
            errorDiv.textContent = 'SKU不能为空';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }
    
    function validateWarehouseSku(input) {
        const errorDiv = document.getElementById('warehouseSkuError');
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbsku1(input) {
        const errorDiv = document.getElementById('sbsku1Error');
        const serverErrorDiv = document.getElementById('sbsku1ServerError'); // ✅ 唯一 ID
        if (input.value.trim() === '') {
            errorDiv.textContent = 'SKU不能为空';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbquantity1(input) {
        const errorDiv = document.getElementById('sbquantity1Error');
        if (input.value.trim() === '' || parseInt(input.value) <= 0) {
            errorDiv.textContent = '数量必须大于0';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateQuantity(input) {
        const errorDiv = document.getElementById('quantityError');
        if (input.value.trim() === '' || parseInt(input.value) <= 0) {
            errorDiv.textContent = '数量必须大于0';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbsku2(input) {
        const errorDiv = document.getElementById('sbsku2Error');
        const serverErrorDiv = document.getElementById('sbsku2ServerError'); // ✅ 唯一 ID
        if (input.value.trim() === '') {
            errorDiv.textContent = 'SKU2不能为空';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbquantity2(input) {
        const errorDiv = document.getElementById('sbquantity2Error');
        
        if (input.value.trim() === '' || parseInt(input.value) <= 0) {
            errorDiv.textContent = '数量必须大于0';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbsku3(input) {
        const errorDiv = document.getElementById('sbsku3Error');
        const serverErrorDiv = document.getElementById('sbsku3ServerError'); // ✅ 唯一 ID
        if (input.value.trim() === '') {
            errorDiv.textContent = 'SKU不能为空';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbquantity3(input) {
        const errorDiv = document.getElementById('sbquantity3Error');
        if (input.value.trim() === '' || parseInt(input.value) <= 0) {
            errorDiv.textContent = '数量必须大于0';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbsku4(input) {
        const errorDiv = document.getElementById('sbsku4Error');
        const serverErrorDiv = document.getElementById('sbsku4ServerError'); // ✅ 唯一 ID
        if (input.value.trim() === '') {
            errorDiv.textContent = 'SKU不能为空';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbquantity4(input) {
        const errorDiv = document.getElementById('sbquantity4Error');
        if (input.value.trim() === '' || parseInt(input.value) <= 0) {
            errorDiv.textContent = '数量必须大于0';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbsku5(input) {
        const errorDiv = document.getElementById('sbsku5Error');
        const serverErrorDiv = document.getElementById('sbsku5ServerError'); // ✅ 唯一 ID
        if (input.value.trim() === '') {
            errorDiv.textContent = 'SKU不能为空';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function validateSbquantity5(input) {
        const errorDiv = document.getElementById('sbquantity5Error');
        if (input.value.trim() === '' || parseInt(input.value) <= 0) {
            errorDiv.textContent = '数量必须大于0';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }
        errorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        return true;
    }

    function toggleAdditionalSkuSection() {
        const additionalSection = document.getElementById('additionalSkuSection');
        const toggleText = document.getElementById('toggleText');
        
        if (additionalSection.style.display === 'none') {
            additionalSection.style.display = 'block';
            toggleText.textContent = '收起更多SKU';
        } else {
            additionalSection.style.display = 'none';
            toggleText.textContent = '展开更多SKU';
        }
    }
    function validateWarehouseSku(input) {
        const errorDiv = document.getElementById('warehouseSkuError');
        const serverErrorDiv = document.getElementById('warehouseSkuServerError');
        const warehouseSku = input.value.trim();

        errorDiv.style.display = 'none';
        serverErrorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        // 显示加载状态
        const statusIcon = document.createElement('span');
        statusIcon.style.color = '#409eff';
        statusIcon.innerHTML = '⏱️ 检查中...';
        input.parentNode.appendChild(statusIcon);

        fetch('<%= request.getContextPath() %>/CheckWarehouseSkuServlet?warehouseSku=' + encodeURIComponent(warehouseSku))
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    statusIcon.innerHTML = '<span style="color:green">✅ 有效</span>';
                    serverErrorDiv.style.display = 'none';
                } else {
                    statusIcon.innerHTML = '<span style="color:red">⚠️ 未找到</span>';
                    serverErrorDiv.textContent = '该仓库SKU不存在于数据库中';
                    serverErrorDiv.style.display = 'block';
                    input.classList.add('is-invalid');
                }
                statusIcon.remove();
            })
            .catch(error => {
                console.error('仓库SKU验证失败:', error);
                statusIcon.innerHTML = '<span style="color:red">❌ 校验失败</span>';
                statusIcon.remove();
            });

        return true;
    }
    document.getElementById('editForm').addEventListener('submit', function (e) {
        const fields = ['warehouseSku', 'sbsku1', 'sbsku2', 'sbsku3', 'sbsku4', 'sbsku5'];
        let hasError = false;

        // 校验卖家名称
        const sellerNameInput = document.getElementById('sellerName');
        const sellerNameServerErrorDiv = document.getElementById('sellerNameServerError');
        if (sellerNameInput && sellerNameInput.value.trim() !== '' && sellerNameServerErrorDiv && sellerNameServerErrorDiv.style.display === 'block') {
            e.preventDefault();
            showErrorMessage('该名字未在数据库中，请联系系统管理员');
            hasError = true;
        }
        fields.forEach(fieldName => {
            const input = document.getElementById(fieldName);
            const serverErrorDiv = document.getElementById(fieldName + 'ServerError'); // ✅ 正确做法！
            if (input && input.value.trim() !== '' && serverErrorDiv && serverErrorDiv.style.display === 'block') {
                e.preventDefault();
                showErrorMessage('仓库SKU校验未通过，请检查输入');
                hasError = true;
            }
        });

        if (hasError) {
            e.preventDefault();
        }
    });

    function validateSbsku1(input) {
        const errorDiv = document.getElementById('sbsku1Error');
        const serverErrorDiv = document.getElementById('sbsku1ServerError');
        const warehouseSku = input.value.trim();

        errorDiv.style.display = 'none';
        serverErrorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        // 显示加载状态
        const statusIcon = document.createElement('span');
        statusIcon.style.color = '#409eff';
        statusIcon.innerHTML = '⏱️ 检查中...';
        input.parentNode.appendChild(statusIcon);

        fetch('<%= request.getContextPath() %>/CheckWarehouseSkuServlet?warehouseSku=' + encodeURIComponent(warehouseSku))
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    statusIcon.innerHTML = '<span style="color:green">✅ 有效</span>';
                    serverErrorDiv.style.display = 'none';
                } else {
                    statusIcon.innerHTML = '<span style="color:red">⚠️ 未找到</span>';
                    serverErrorDiv.textContent = '该仓库SKU不存在于数据库中';
                    serverErrorDiv.style.display = 'block';
                    input.classList.add('is-invalid');
                }
                statusIcon.remove();
            })
            .catch(error => {
                console.error('仓库SKU验证失败:', error);
                statusIcon.innerHTML = '<span style="color:red">❌ 校验失败</span>';
                serverErrorDiv.textContent = '系统繁忙，请稍后重试';
                serverErrorDiv.style.display = 'block';
                statusIcon.remove();
            });

        return true;
    }

    // 为 sbsku2 到 sbsku5 重复定义 validateSbsku2 到 validateSbsku5 函数
    // 示例：validateSbsku2
    function validateSbsku2(input) {
        const errorDiv = document.getElementById('sbsku2Error');
        const serverErrorDiv = document.getElementById('sbsku2ServerError');
        const warehouseSku = input.value.trim();

        errorDiv.style.display = 'none';
        serverErrorDiv.style.display = 'none';
        input.classList.remove('is-invalid');

        if (!warehouseSku) {
            errorDiv.textContent = '仓库SKU不能为空';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }

        // 显示加载状态
        const statusIcon = document.createElement('span');
        statusIcon.style.color = '#409eff';
        statusIcon.innerHTML = '⏱️ 检查中...';
        input.parentNode.appendChild(statusIcon);

        fetch('<%= request.getContextPath() %>/CheckWarehouseSkuServlet?warehouseSku=' + encodeURIComponent(warehouseSku))
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    statusIcon.innerHTML = '<span style="color:green">✅ 有效</span>';
                    serverErrorDiv.style.display = 'none';
                } else {
                    statusIcon.innerHTML = '<span style="color:red">⚠️ 未找到</span>';
                    serverErrorDiv.textContent = '该仓库SKU不存在于数据库中';
                    serverErrorDiv.style.display = 'block';
                    input.classList.add('is-invalid');
                }
                statusIcon.remove();
            })
            .catch(error => {
                console.error('仓库SKU验证失败:', error);
                statusIcon.innerHTML = '<span style="color:red">❌ 校验失败</span>';
                serverErrorDiv.textContent = '系统繁忙，请稍后重试';
                serverErrorDiv.style.display = 'block';
                statusIcon.remove();
            });

        return true;
    }

    // 依此类推：validateSbsku3、validateSbsku4、validateSbsku5

 // validateSbsku3
    function validateSbsku3(input) {
        const errorDiv = document.getElementById('sbsku3Error');
        const serverErrorDiv = document.getElementById('sbsku3ServerError');
        const warehouseSku = input.value.trim();

        errorDiv.style.display = 'none';
        serverErrorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        // 显示加载状态
        const statusIcon = document.createElement('span');
        statusIcon.style.color = '#409eff';
        statusIcon.innerHTML = '⏱️ 检查中...';
        input.parentNode.appendChild(statusIcon);

        fetch('<%= request.getContextPath() %>/CheckWarehouseSkuServlet?warehouseSku=' + encodeURIComponent(warehouseSku))
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    statusIcon.innerHTML = '<span style="color:green">✅ 有效</span>';
                    serverErrorDiv.style.display = 'none';
                } else {
                    statusIcon.innerHTML = '<span style="color:red">⚠️ 未找到</span>';
                    serverErrorDiv.textContent = '该仓库SKU不存在于数据库中';
                    serverErrorDiv.style.display = 'block';
                    input.classList.add('is-invalid');
                }
                statusIcon.remove();
            })
            .catch(error => {
                console.error('仓库SKU验证失败:', error);
                statusIcon.innerHTML = '<span style="color:red">❌ 校验失败</span>';
                serverErrorDiv.textContent = '系统繁忙，请稍后重试';
                serverErrorDiv.style.display = 'block';
                statusIcon.remove();
            });

        return true;
    }

    // validateSbsku4
    function validateSbsku4(input) {
        const errorDiv = document.getElementById('sbsku4Error');
        const serverErrorDiv = document.getElementById('sbsku4ServerError');
        const warehouseSku = input.value.trim();

        errorDiv.style.display = 'none';
        serverErrorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        // 显示加载状态
        const statusIcon = document.createElement('span');
        statusIcon.style.color = '#409eff';
        statusIcon.innerHTML = '⏱️ 检查中...';
        input.parentNode.appendChild(statusIcon);

        fetch('<%= request.getContextPath() %>/CheckWarehouseSkuServlet?warehouseSku=' + encodeURIComponent(warehouseSku))
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    statusIcon.innerHTML = '<span style="color:green">✅ 有效</span>';
                    serverErrorDiv.style.display = 'none';
                } else {
                    statusIcon.innerHTML = '<span style="color:red">⚠️ 未找到</span>';
                    serverErrorDiv.textContent = '该仓库SKU不存在于数据库中';
                    serverErrorDiv.style.display = 'block';
                    input.classList.add('is-invalid');
                }
                statusIcon.remove();
            })
            .catch(error => {
                console.error('仓库SKU验证失败:', error);
                statusIcon.innerHTML = '<span style="color:red">❌ 校验失败</span>';
                serverErrorDiv.textContent = '系统繁忙，请稍后重试';
                serverErrorDiv.style.display = 'block';
                statusIcon.remove();
            });

        return true;
    }

    // validateSbsku5
    function validateSbsku5(input) {
        const errorDiv = document.getElementById('sbsku5Error');
        const serverErrorDiv = document.getElementById('sbsku5ServerError');
        const warehouseSku = input.value.trim();

        errorDiv.style.display = 'none';
        serverErrorDiv.style.display = 'none';
        input.classList.remove('is-invalid');
        // 显示加载状态
        const statusIcon = document.createElement('span');
        statusIcon.style.color = '#409eff';
        statusIcon.innerHTML = '⏱️ 检查中...';
        input.parentNode.appendChild(statusIcon);

        fetch('<%= request.getContextPath() %>/CheckWarehouseSkuServlet?warehouseSku=' + encodeURIComponent(warehouseSku))
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    statusIcon.innerHTML = '<span style="color:green">✅ 有效</span>';
                    serverErrorDiv.style.display = 'none';
                } else {
                    statusIcon.innerHTML = '<span style="color:red">⚠️ 未找到</span>';
                    serverErrorDiv.textContent = '该仓库SKU不存在于数据库中';
                    serverErrorDiv.style.display = 'block';
                    input.classList.add('is-invalid');
                }
                statusIcon.remove();
            })
            .catch(error => {
                console.error('仓库SKU验证失败:', error);
                statusIcon.innerHTML = '<span style="color:red">❌ 校验失败</span>';
                serverErrorDiv.textContent = '系统繁忙，请稍后重试';
                serverErrorDiv.style.display = 'block';
                statusIcon.remove();
            });

        return true;
    }
    function validateSellerName(input) {
        const errorDiv = document.getElementById('sellerNameError');
        const serverErrorDiv = document.getElementById('sellerNameServerError');
        const sellerName = input.value.trim();

        errorDiv.style.display = 'none';
        serverErrorDiv.style.display = 'none';
        input.classList.remove('is-invalid');

        if (!sellerName) {
            errorDiv.textContent = '卖家名称不能为空';
            errorDiv.style.display = 'block';
            input.classList.add('is-invalid');
            return false;
        }

        // 显示加载状态
        const statusIcon = document.createElement('span');
        statusIcon.style.color = '#409eff';
        statusIcon.innerHTML = '⏱️ 检查中...';
        input.parentNode.appendChild(statusIcon);

        fetch('<%= request.getContextPath() %>/CheckSellerNameServlet?sellerName=' + encodeURIComponent(sellerName))
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    statusIcon.innerHTML = '<span style="color:green">✅ 有效</span>';
                    serverErrorDiv.style.display = 'none';
                } else {
                    statusIcon.innerHTML = '<span style="color:red">⚠️ 未找到</span>';
                    serverErrorDiv.textContent = '该名字未在数据库中，请联系系统管理员';
                    serverErrorDiv.style.display = 'block';
                    input.classList.add('is-invalid');
                }
                statusIcon.remove();
            })
            .catch(error => {
                console.error('卖家名称验证失败:', error);
                statusIcon.innerHTML = '<span style="color:red">❌ 校验失败</span>';
                serverErrorDiv.textContent = '系统繁忙，请稍后重试';
                serverErrorDiv.style.display = 'block';
                statusIcon.remove();
            });

        return true;
    }

    function checkSkuExists(skuInput) {
        const sku = skuInput.value.trim();
        const errorDiv = document.getElementById('skuError');
        const serverErrorDiv = document.getElementById('serverError');
        const warehouseSection = document.getElementById('warehouseSkuSection');
        const sellerNameSection = document.getElementById('sellerNameSection'); // ✅ 新增引用
        errorDiv.style.display = 'none';
        serverErrorDiv.style.display = 'none';
        skuInput.classList.remove('is-invalid');
        
        if (!sku) {
            errorDiv.textContent = '';
            errorDiv.style.display = 'block';
            skuInput.classList.add('is-invalid');
            warehouseSection.style.display = 'none';
            sellerNameSection.style.display = 'none'; // ✅ 隐藏卖家名称
            return;
        }
        
        const statusIcon = document.createElement('span');
        statusIcon.style.color = '#409eff';
        statusIcon.innerHTML = '⏱️ 检查中...';
        skuInput.parentNode.appendChild(statusIcon);
        
        fetch('<%= request.getContextPath() %>/CheckSkuServlet?sku=' + encodeURIComponent(sku))
            .then(response => response.json())
            .then(data => {
                if (data.exists) {
                    statusIcon.innerHTML = '<span style="color:green">✅ SKU有效</span>';
                    skuInput.classList.remove('is-invalid');
                    errorDiv.style.display = 'none';
                    warehouseSection.style.display = 'none';
                    sellerNameSection.style.display = 'none'; // ✅ 隐藏卖家名称
                } else {
                    statusIcon.innerHTML = '<span style="color:red">⚠️ 未找到此SKU</span>';
                    errorDiv.textContent = '该平台SKU不存在于亚马逊商品库中，请补充对应的仓库SKU信息';
                    errorDiv.style.display = 'block';
                    skuInput.classList.add('is-invalid');
                    warehouseSection.style.display = 'block';
                    sellerNameSection.style.display = 'block'; // ✅ 显示卖家名称
                    document.getElementById('currentSku').textContent = sku;
                    if (!document.getElementById('warehouseSku').value) {
                        document.getElementById('warehouseSku').value = '';
                    }
                    if (!document.getElementById('sbsku1').value) {
                        document.getElementById('sbsku1').value = '';
                    }
                }
                statusIcon.remove();
            })
            .catch(error => {
                console.error('校验请求失败:', error);
                statusIcon.innerHTML = '<span style="color:red">❌ 校验失败</span>';
                errorDiv.textContent = '系统繁忙，请稍后重试';
                errorDiv.style.display = 'block';
                sellerNameSection.style.display = 'none'; // ✅ 隐藏卖家名称
                warehouseSection.style.display = 'none';
                statusIcon.remove();
            });
    }
</script>

</body>
</html>