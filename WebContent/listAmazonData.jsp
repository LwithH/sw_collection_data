<%@ page contentType="text/html;charset=UTF-8" %>
<%@ page import="com.model.AmazonData" %>
<%@ page import="java.util.List" %>
<%
    // 构建公共查询参数（用于分页链接）
    String searchField = (String) request.getAttribute("searchField");
    String keyword = (String) request.getAttribute("keyword");
    String searchField2 = (String) request.getAttribute("searchField2");
    String keyword2 = (String) request.getAttribute("keyword2");
    String emptySellerChecked = (String) request.getAttribute("emptySellerChecked");
    Integer selectedYear = (Integer) request.getAttribute("selectedYear");
    int pageSize = (int) request.getAttribute("pageSize");

    StringBuilder paramBuilder = new StringBuilder();
    if (searchField != null && !searchField.isEmpty()) {
        paramBuilder.append("&searchField=").append(java.net.URLEncoder.encode(searchField, "UTF-8"));
    }
    if (keyword != null && !keyword.isEmpty()) {
        paramBuilder.append("&keyword=").append(java.net.URLEncoder.encode(keyword, "UTF-8"));
    }
    if (searchField2 != null && !searchField2.isEmpty()) {
        paramBuilder.append("&searchField2=").append(java.net.URLEncoder.encode(searchField2, "UTF-8"));
    }
    if (keyword2 != null && !keyword2.isEmpty()) {
        paramBuilder.append("&keyword2=").append(java.net.URLEncoder.encode(keyword2, "UTF-8"));
    }
    if ("checked".equals(emptySellerChecked)) {
        paramBuilder.append("&emptySeller=on");
    }
    if (selectedYear != null) {
        paramBuilder.append("&year=").append(selectedYear);
    }
    String commonParams = paramBuilder.toString();
    
    // 获取图表数据
    int totalRecords = (Integer) request.getAttribute("totalRecords");
    int emptySellerCount = (Integer) request.getAttribute("emptySellerCount");
    int filledSellerCount = totalRecords - emptySellerCount;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>亚马逊数据表</title>
    <script src="./js/lib/Chart.min.js"></script>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background: #f5f5f5; }
        .container { background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #f8f9fa; }
        
        /* 分页导航样式 */
        .pagination { margin-top: 20px; text-align: center; }
        .pagination .page-size-select, 
        .pagination a, .pagination span, 
        .pagination .page-info, .pagination #jumpPage, .pagination .jump-btn {
            display: inline-block; 
            padding: 6px 12px; 
            margin: 0 4px;
            text-decoration: none; 
            border: 1px solid #ddd;
            vertical-align: middle; 
            box-sizing: border-box;
        }
        .pagination .page-size-select select {
            padding: 4px 6px;
            border: 1px solid #ddd;
            border-radius: 4px;
            margin: 0 4px;
            vertical-align: middle;
        }
        .pagination .page-size-select {
            border: none;
            padding: 6px 0;
        }
        .pagination .current { background: #409eff; color: white; border-color: #409eff; }
        .pagination .page-info {
            border: none; padding: 6px 8px; margin: 0 6px;
        }
        .pagination #jumpPage {
            width: 60px; padding: 6px 8px; text-align: center;
        }
        .pagination .jump-btn {
            background: #409eff; color: white; border-color: #409eff;
            cursor: pointer; padding: 6px 12px;
        }
        .pagination .jump-btn:hover { background: #3390e0; }
        
        .home-link {
            color: #409eff;
            text-decoration: none;
            font-weight: bold;
            display: inline-flex;
            align-items: center;
            margin-bottom: 15px;
        }
        .home-link:hover { color: #3390e0; }
        .home-link::before { content: "← "; }
        
        .title-highlight {
            color: #333; 
            font-size: 24px; 
            font-weight: 600; 
            border-bottom: 3px solid #409eff;
            padding-bottom: 6px; 
            display: block;
            width: 100%;
            box-sizing: border-box;
        }
        .title-container {
            text-align: left; 
            margin: 0 0 20px 0; 
            width: 100%;
            box-sizing: border-box;
        }

        .text-ellipsis {
            max-width: 200px;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }

        .edit-btn {
            padding: 4px 8px;
            background: #67c23a;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-size: 12px;
        }
        .edit-btn:hover {
            background: #5daf34;
        }

        input[readonly] {
            background-color: #f5f5f5 !important;
            color: #666 !important;
            font-style: italic;
            cursor: not-allowed;
        }

        #toast {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            background: #67c23a;
            color: white;
            padding: 12px 24px;
            border-radius: 6px;
            box-shadow: 0 6px 20px rgba(0,0,0,0.2);
            z-index: 2000;
            display: none;
            font-size: 16px;
            font-weight: bold;
            text-align: center;
            min-width: 200px;
        }
        #toast.error {
            background: #f56c6c;
        }

        #editModal > div,
        #batchEditModal > div,
        #addModal > div {
            width: 600px;
            max-width: 90%;
            padding: 25px;
        }

     /* 统一所有非隐藏输入框和下拉框的样式 */
#editForm input:not([type="hidden"]),
#editForm select,
#batchEditForm input:not([type="hidden"]),
#batchEditForm select,
#addForm input:not([type="hidden"]),
#addForm select {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid #ddd;
    border-radius: 4px;
    font-size: 14px;
    min-height: 36px;
    box-sizing: border-box;
}

        /* 新增下拉搜索框样式 */
        .search-select-container {
            position: relative;
        }
        .search-select-input {
            width: 100%;
            padding: 8px 12px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
        }
        .search-select-dropdown {
            position: absolute;
            width: 100%;
            max-height: 200px;
            overflow-y: auto;
            border: 1px solid #ddd;
            border-radius: 4px;
            background: white;
            z-index: 1001;
            display: none;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
            margin-top: 4px;
        }
        .search-select-option {
            padding: 8px 12px;
            cursor: pointer;
        }
        .search-select-option:hover {
            background-color: #f5f5f5;
        }
        .search-select-no-results {
            padding: 8px 12px;
            color: #666;
            text-align: center;
        }
        
        .chart-container {
            margin: 20px 0; 
            padding: 15px; 
            background: #ffffff; 
            border-radius: 6px; 
            box-shadow: 0 2px 6px rgba(0,0,0,0.05);
            position: relative;
            height: 300px;
        }
        
        .stats-summary {
            display: flex;
            justify-content: space-around;
            margin: 15px 0;
            padding: 15px;
            background: #f8f9fa;
            border-radius: 6px;
        }
        .stat-item {
            text-align: center;
        }
        .stat-number {
            font-size: 24px;
            font-weight: bold;
            color: #409eff;
        }
        .stat-label {
            font-size: 14px;
            color: #666;
        }

        /* 批量按钮样式 */
        #batchEditBtn, #addNewBtn {
            padding: 6px 12px;
            border: none;
            border-radius: 4px;
            cursor: pointer;
            font-weight: bold;
            margin: 10px 5px 10px 0;
        }
        #batchEditBtn {
            background: #e6a23c;
            color: white;
        }
        #batchEditBtn:hover {
            background: #d9912f;
        }
        #addNewBtn {
            background: #409eff;
            color: white;
        }
        #addNewBtn:hover {
            background: #3390e0;
        }

        /* Seller验证样式 */
        .seller-valid {
            border-color: #67c23a !important;
        }

        .seller-invalid {
            border-color: #f56c6c !important;
        }

        .sku-valid {
            border-color: #67c23a !important;
        }

        .sku-invalid {
            border-color: #f56c6c !important;
        }

        #sellerValidationHint,
        #skuValidationHint {
            font-size: 12px;
            margin-top: 4px;
            padding: 4px 8px;
            border-radius: 4px;
            transition: all 0.3s;
        }
    </style>
</head>
<body>
<div class="container">
    <a href="index.jsp" class="home-link">返回首页</a>

    <div class="title-container">
        <h2><span class="title-highlight">亚马逊-列表</span></h2>
    </div>

    <!-- 搜索区域 -->
    <div style="margin: 15px 0; padding: 15px; background: #f8f9fa; border-radius: 6px;">
        <form method="get" action="ListAmazonDataServlet" id="searchForm" 
              style="display: flex; flex-wrap: wrap; gap: 12px; align-items: end;">
            <div>
                <label>字段:</label>
                <select name="searchField">
                    <option value="">-- 请选择 --</option>
                    <option value="id" <%= "id".equals(searchField) ? "selected" : "" %>>ID</option>
                    <option value="uacs" <%= "uacs".equals(searchField) ? "selected" : "" %>>账户</option>
                    <option value="sku" <%= "sku".equals(searchField) ? "selected" : "" %>>SKU</option>
                    <option value="title" <%= "title".equals(searchField) ? "selected" : "" %>>标题</option>
                    <option value="seller" <%= "seller".equals(searchField) ? "selected" : "" %>>Seller</option>
                    <option value="sales_depart" <%= "sales_depart".equals(searchField) ? "selected" : "" %>>Sales Depart</option>
                    <option value="asin" <%= "asin".equals(searchField) ? "selected" : "" %>>ASIN</option>
                    <option value="parent_asin" <%= "parent_asin".equals(searchField) ? "selected" : "" %>>Parent ASIN</option>
                    <option value="lifecl" <%= "lifecl".equals(searchField) ? "selected" : "" %>>Lifecl</option>
                </select>
            </div>

            <div>
                <label>关键词:</label>
                <input type="text" name="keyword" value="<%= keyword != null ? keyword : "" %>" 
                       placeholder="输入搜索内容">
            </div>
<!-- 第二个搜索框 -->
<div>
    <label>字段:</label>
    <select name="searchField2">
        <option value="">-- 请选择 --</option>
        <option value="id" <%= "id".equals(request.getAttribute("searchField2")) ? "selected" : "" %>>ID</option>
        <option value="uacs" <%= "uacs".equals(request.getAttribute("searchField2")) ? "selected" : "" %>>账户</option>
        <option value="sku" <%= "sku".equals(request.getAttribute("searchField2")) ? "selected" : "" %>>SKU</option>
        <option value="title" <%= "title".equals(request.getAttribute("searchField2")) ? "selected" : "" %>>标题</option>
        <option value="seller" <%= "seller".equals(request.getAttribute("searchField2")) ? "selected" : "" %>>Seller</option>
        <option value="sales_depart" <%= "sales_depart".equals(request.getAttribute("searchField2")) ? "selected" : "" %>>Sales Depart</option>
        <option value="asin" <%= "asin".equals(request.getAttribute("searchField2")) ? "selected" : "" %>>ASIN</option>
        <option value="parent_asin" <%= "parent_asin".equals(request.getAttribute("searchField2")) ? "selected" : "" %>>Parent ASIN</option>
        <option value="lifecl" <%= "lifecl".equals(request.getAttribute("searchField2")) ? "selected" : "" %>>Lifecl</option>
    </select>
</div>

<div>
    <label>关键词:</label>
    <input type="text" name="keyword2" value="<%= request.getAttribute("keyword2") != null ? request.getAttribute("keyword2") : "" %>" 
           placeholder="输入搜索内容">
</div>

            <div>
                <label>
                    <input type="checkbox" name="emptySeller" value="on" 
                           <%= "checked".equals(emptySellerChecked) ? "checked" : "" %>
                           onchange="this.form.submit()">
                    Seller 为空
                </label>
            </div>

            <div>
                <label>年份:</label>
                <select name="year" onchange="this.form.submit()">
                    <option value="">-- 全部 --</option>
                    <%
                        int currentYear = java.time.Year.now().getValue();
                        for (int y = currentYear; y >= currentYear - 5; y--) {
                    %>
                        <option value="<%=y%>" <%= selectedYear != null && selectedYear == y ? "selected" : "" %>><%=y%></option>
                    <%
                        }
                    %>
                </select>
            </div>

            <button type="submit" style="padding: 6px 12px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer;">
                搜索
            </button>

            <button type="button" onclick="location.href='ListAmazonDataServlet?page=1&size=10'"
                    style="padding: 6px 12px; background: #606266; color: white; border: none; border-radius: 4px; cursor: pointer;">
                重置
            </button>

            <input type="hidden" name="page" value="1">
            <input type="hidden" name="size" value="<%= pageSize %>">
        </form>
    </div>

    <!-- 操作按钮区域 -->
    <div style="margin: 10px 0 15px;">
        <button id="addNewBtn">+ 新增记录</button>
        <button id="batchEditBtn">批量修改（当前页）</button>
    </div>

    <!-- 统计摘要 -->
    <div class="stats-summary">
        <div class="stat-item">
            <div class="stat-number"><%= totalRecords %></div>
            <div class="stat-label">总记录数</div>
        </div>
        <div class="stat-item">
            <div class="stat-number"><%= filledSellerCount %></div>
            <div class="stat-label">Seller已填写</div>
        </div>
        <div class="stat-item">
            <div class="stat-number"><%= emptySellerCount %></div>
            <div class="stat-label">Seller为空</div>
        </div>
        <div class="stat-item">
            <div class="stat-number"><%= totalRecords > 0 ? String.format("%.1f", (emptySellerCount * 100.0 / totalRecords)) : "0" %>%</div>
            <div class="stat-label">空值占比</div>
        </div>
    </div>

    <!-- 图表 -->
    <div class="chart-container">
        <canvas id="sellerChart"></canvas>
    </div>

    <!-- 数据表格 -->
    <table>
        <thead>
            <tr>
                <th><input type="checkbox" id="selectAll"></th>
                <th>ID</th>
                <th>账户</th>
                <th>SKU</th>
                <th>标题</th>
                <th>Seller</th>
                <th>部门</th>
                <th>ASIN</th>
                <th>Parent ASIN</th>
                <th>Lifecl</th>
                <th>操作</th>
            </tr>
        </thead>
        <tbody>
            <%
                List<AmazonData> dataList = (List<AmazonData>) request.getAttribute("dataList");
                if (dataList != null && !dataList.isEmpty()) {
                    for (AmazonData d : dataList) {
            %>
            <tr data-id="<%= d.getId() %>">
                <td>
                    <input type="checkbox" class="row-checkbox" 
                           data-id="<%= d.getId() %>"
                           data-seller="<%= d.getSeller() != null ? d.getSeller() : "" %>"
                           data-lifecl="<%= d.getLifecl() != null ? d.getLifecl() : "" %>">
                </td>
                <td><%= d.getId() %></td>
                <td><%= d.getUacs() != null ? d.getUacs() : "-" %></td>
                <td><%= d.getSku() != null ? d.getSku() : "-" %></td>
                <td class="text-ellipsis" title="<%= d.getTitle() != null ? d.getTitle().replace("\"", "&quot;") : "" %>">
                    <%= d.getTitle() != null ? d.getTitle() : "-" %>
                </td>
                <td><%= d.getSeller() != null ? d.getSeller() : "-" %></td>
                <td>
                    <%
                        String salesDepart = d.getSalesDepart();
                        String departName = "-";
                        if (salesDepart != null && !salesDepart.isEmpty()) {
                            try {
                                int departCode = Integer.parseInt(salesDepart);
                                if (departCode == 0) {
                                    departName = "-";
                                } else {
                                    switch(departCode) {
                                        case 1: departName = "销售一部"; break;
                                        case 2: departName = "销售二部"; break;
                                        case 3: departName = "乐器项目部"; break;
                                        case 4: departName = "大件项目部"; break;
                                        case 5: departName = "销售三部"; break;
                                        case 6: departName = "天津项目部"; break;
                                        case 7: departName = "工业项目部"; break;
                                        case 97: departName = "Ali"; break;
                                        case 98: departName = "深圳公司"; break;
                                        case 99: departName = "停用"; break;
                                        default: departName = "未知部门(" + departCode + ")"; break;
                                    }
                                }
                            } catch (NumberFormatException e) {
                                if ("0".equals(salesDepart)) {
                                    departName = "-";
                                } else {
                                    departName = "未知部门(" + salesDepart + ")";
                                }
                            }
                        }
                        out.print(departName);
                    %>
                </td>
                <td><%= d.getAsin() != null ? d.getAsin() : "-" %></td>
                <td><%= d.getParentAsin() != null ? d.getParentAsin() : "-" %></td>
                <td><%= d.getLifecl() != null ? d.getLifecl() : "-" %></td>
                <td>
                    <button class="edit-btn" 
                            data-id="<%= d.getId() %>"
                            data-sku="<%= d.getSku() != null ? d.getSku() : "" %>"
                            data-uacs="<%= d.getUacs() != null ? d.getUacs() : "" %>"
                            data-seller="<%= d.getSeller() != null ? d.getSeller() : "" %>"
                            data-lifecl="<%= d.getLifecl() != null ? d.getLifecl() : "" %>">
                        修改
                    </button>
                </td>
            </tr>
            <%
                    }
                } else {
            %>
            <tr><td colspan="11" style="text-align: center;">暂无数据</td></tr>
            <%
                }
            %>
        </tbody>
    </table>

    <!-- 分页 -->
    <div class="pagination">
        <%
            int currentPage = (int) request.getAttribute("currentPage");
            int totalPages = (int) request.getAttribute("totalPages");
            int total = (int) request.getAttribute("total");
            if (pageSize == 0) pageSize = 10;
        %>

        <div class="page-size-select">
            每页显示：
            <select onchange="location.href='ListAmazonDataServlet?page=1&size='+this.value+'<%=commonParams%>'">
                <option value="10" <%= pageSize == 10 ? "selected" : "" %>>10</option>
                <option value="20" <%= pageSize == 20 ? "selected" : "" %>>20</option>
                <option value="50" <%= pageSize == 50 ? "selected" : "" %>>50</option>
                <option value="100" <%= pageSize == 100 ? "selected" : "" %>>100</option>
                <option value="200" <%= pageSize == 200 ? "selected" : "" %>>200</option>
            </select>
            条
        </div>

        <% if (currentPage > 1) { %>
            <a href="ListAmazonDataServlet?page=1&size=<%=pageSize%><%=commonParams%>">首页</a>
            <a href="ListAmazonDataServlet?page=<%=currentPage - 1%>&size=<%=pageSize%><%=commonParams%>">上一页</a>
        <% } %>

        <%
            int start = Math.max(1, currentPage - 2);
            int end = Math.min(totalPages, currentPage + 2);
            for (int i = start; i <= end; i++) {
                if (i == currentPage) {
        %>
            <span class="current"><%=i%></span>
        <% } else { %>
            <a href="ListAmazonDataServlet?page=<%=i%>&size=<%=pageSize%><%=commonParams%>"><%=i%></a>
        <% } } %>

        <% if (currentPage < totalPages) { %>
            <a href="ListAmazonDataServlet?page=<%=currentPage + 1%>&size=<%=pageSize%><%=commonParams%>">下一页</a>
            <a href="ListAmazonDataServlet?page=<%=totalPages%>&size=<%=pageSize%><%=commonParams%>">末页</a>
        <% } %>

        <span class="page-info">共 <%=total%> 条记录，<%=totalPages%> 页，当前第 <%=currentPage%> 页</span>

        <span class="page-info">跳至：</span>
        <input type="number" id="jumpPage" min="1" max="<%=totalPages%>" value="<%=currentPage%>">
        <button class="jump-btn" onclick="jumpToPage(<%=pageSize%>, '<%=commonParams%>')">跳转</button>
    </div>
</div>

<!-- 单条修改模态框 -->
<div id="editModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center;">
    <div style="background: white; padding: 20px; border-radius: 8px; width: 400px; max-width: 90%;">
        <h3 style="margin-top: 0;">修改数据</h3>
        <form id="editForm">
            <input type="hidden" id="editId">
            <div style="margin-bottom: 12px;">
                <label>ID:</label>
                <input type="text" id="editIdDisplay" readonly>
            </div>
            <div style="margin-bottom: 12px;">
                <label>SKU:</label>
                <input type="text" id="editSku" readonly>
            </div>
            <div style="margin-bottom: 12px;">
                <label>账户:</label>
                <input type="text" id="editUacs" readonly>
            </div>
            <div style="margin-bottom: 12px;">
                <label>Seller:</label>
                <input type="text" id="editSeller" data-original-seller="">
            </div>
            <div style="margin-bottom: 12px;">
                <label>Lifecl:</label>
                <select id="editLifecl">
                    <option value="">--(空白) --</option>
                    <option value="新品期">新品期</option>
                    <option value="测试期">测试期</option>
                    <option value="成熟期">成熟期</option>
                    <option value="淘汰期">淘汰期</option>
                    <option value="推广期">推广期</option>
                    <option value="已清完">已清完</option>
                </select>
            </div>
            <div style="text-align: right;">
                <button type="button" id="cancelEdit" style="padding: 6px 12px; background: #606266; color: white; border: none; border-radius: 4px; margin-right: 8px;">取消</button>
                <button type="submit" style="padding: 6px 12px; background: #409eff; color: white; border: none; border-radius: 4px;">保存</button>
            </div>
        </form>
    </div>
</div>

<!-- 批量修改模态框 -->
<div id="batchEditModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center;">
    <div style="background: white; padding: 25px; border-radius: 8px; width: 500px; max-width: 90%;">
        <h3 style="margin-top: 0;">批量修改（共 <span id="selectedCount">0</span> 条）</h3>
        <form id="batchEditForm">
            <div style="margin-bottom: 15px;">
                <label>
                    <input type="checkbox" id="updateSeller"> 修改 Seller
                </label>
                <input type="text" id="batchSeller" placeholder="输入新的 Seller 值" style="display: none; margin-top: 6px;">
            </div>

            <div style="margin-bottom: 15px;">
                <label>
                    <input type="checkbox" id="updateLifecl"> 修改 Lifecl
                </label>
                <select id="batchLifecl" style="display: none; margin-top: 6px;">
                    <option value="">-- 选择状态 --</option>
                    <option value="新品期">新品期</option>
                    <option value="测试期">测试期</option>
                    <option value="成熟期">成熟期</option>
                    <option value="淘汰期">淘汰期</option>
                    <option value="推广期">推广期</option>
                    <option value="已清完">已清完</option>
                </select>
            </div>

            <div style="text-align: right; margin-top: 20px;">
                <button type="button" id="cancelBatchEdit" style="padding: 8px 16px; background: #606266; color: white; border: none; border-radius: 4px; margin-right: 8px;">取消</button>
                <button type="submit" style="padding: 8px 16px; background: #e6a23c; color: white; border: none; border-radius: 4px;">确认批量修改</button>
            </div>
        </form>
    </div>
</div>

<!-- 新增记录模态框 -->
<div id="addModal" style="display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center;">
    <div style="background: white; padding: 25px; border-radius: 8px; width: 500px; max-width: 90%;">
        <h3 style="margin-top: 0;">新增亚马逊记录</h3>
            <!-- 新增：批量新增按钮 -->
        <div style="margin-bottom: 15px; text-align: center;">
            <button type="button" id="toggleBatchMode" style="padding: 6px 12px; background: #e6a23c; color: white; border: none; border-radius: 4px; cursor: pointer;">
                切换到批量新增模式
            </button>
        </div>
       <form id="addForm">
            <!-- 单条新增模式 - 账户字段 -->
            <div id="singleUacsField" style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px;">账户 <span style="color: red;">*</span></label>
                <div class="search-select-container">
                    <input type="text" id="searchUacs" class="search-select-input" placeholder="搜索账户...">
                    <div id="uacsDropdown" class="search-select-dropdown"></div>
                    <input type="hidden" id="selectedUacs">
                </div>
            </div>
            
            <!-- 批量新增模式 - 账户前缀字段 -->
            <div id="batchUacsField" style="margin-bottom: 15px; display: none;">
                <label style="display: block; margin-bottom: 5px;">批量新增账户前缀 <span style="color: red;">*</span></label>
                <div class="search-select-container">
                    <input type="text" id="searchUacsPrefix" class="search-select-input" placeholder="搜索账户前缀...">
                    <div id="uacsPrefixDropdown" class="search-select-dropdown"></div>
                    <input type="hidden" id="selectedUacsPrefix">
                </div>
                <div id="batchUacsPreview" style="margin-top: 8px; font-size: 12px; color: #666; display: none;">
                    <strong>将新增的账户：</strong>
                    <span id="previewUacsList"></span>
                </div>
            </div>
            
            <!-- 其他字段保持不变 -->
            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px;">SKU <span style="color: red;">*</span></label>
                <input type="text" id="addSku" placeholder="请输入平台SKU">
                <!-- SKU重复性验证提示区域 -->
                <div id="skuValidationHint"></div>
            </div>
            
            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px;">仓库SKU</label>
                <input type="text" id="addWarehouseSku" placeholder="最好能补充仓库SKU">
            </div>
            
            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px;">Seller <span style="color: red;">*</span></label>
                <input type="text" id="addSeller" placeholder="请输入Seller名称">
                <!-- Seller验证提示区域 -->
                <div id="sellerValidationHint"></div>
            </div>
            
            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px;">ASIN</label>
                <input type="text" id="addAsin" placeholder="请输入ASIN">
            </div>
            
            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px;">Parent ASIN</label>
                <input type="text" id="addParentAsin" placeholder="请输入Parent ASIN">
            </div>
            
            <div style="margin-bottom: 15px;">
                <label style="display: block; margin-bottom: 5px;">生命周期</label>
                <select id="addLifecl">
                    <option value="">--(空白)--</option>
                    <option value="新品期">新品期</option>
                    <option value="测试期">测试期</option>
                    <option value="成熟期">成熟期</option>
                    <option value="淘汰期">淘汰期</option>
                    <option value="推广期">推广期</option>
                    <option value="已清完">已清完</option>
                </select>
            </div>
            
            <div style="text-align: right; margin-top: 20px;">
                <button type="button" id="cancelAdd" style="padding: 8px 16px; background: #606266; color: white; border: none; border-radius: 4px; margin-right: 8px;">取消</button>
                <button type="submit" id="submitAdd" style="padding: 8px 16px; background: #409eff; color: white; border: none; border-radius: 4px;">添加记录</button>
            </div>
        </form>
    </div>
</div>

<!-- Toast -->
<div id="toast"></div>

<script>
// 显示 Toast
function showToast(message, isError) {
    var toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = isError ? 'error' : '';
    toast.style.display = 'block';
    setTimeout(function() {
        toast.style.display = 'none';
    }, 2000);
}

// 图表
window.addEventListener('DOMContentLoaded', function() {
    const ctx = document.getElementById('sellerChart').getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: ['Seller 已填写', 'Seller 未填写'],
            datasets: [{
                data: [<%= filledSellerCount %>, <%= emptySellerCount %>],
                backgroundColor: ['#4CAF50', '#F44336'],
                borderColor: ['#388E3C', '#D32F2F'],
                borderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: { position: 'right' },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.label || '';
                            if (label) label += ': ';
                            const total = <%= totalRecords %>;
                            const value = context.raw;
                            const percent = ((value / total) * 100).toFixed(2);
                            return label + `${value} (${percent}%)`;
                        }
                    }
                },
                title: {
                    display: true,
                    text: 'Seller 填写情况分布'
                }
            }
        }
    });
    
    // ============== 新增功能相关代码 ==============
    // 获取UACS列表
    fetch('GetUacsListServlet')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                window.uacsList = data.uacsList;
            } else {
                showToast('❌ 获取账户列表失败', true);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('❌ 获取账户列表时出错', true);
        });
    
    // UACS搜索功能
    const searchUacs = document.getElementById('searchUacs');
    const uacsDropdown = document.getElementById('uacsDropdown');
    
    searchUacs.addEventListener('input', function() {
        const searchTerm = this.value.toLowerCase();
        
        if (searchTerm.length === 0) {
            uacsDropdown.style.display = 'none';
            return;
        }
        
        // 过滤UACS列表
        const filteredUacs = window.uacsList.filter(uacs => 
            uacs.toLowerCase().includes(searchTerm)
        );
        
        // 显示下拉菜单
        uacsDropdown.innerHTML = '';
        if (filteredUacs.length > 0) {
            filteredUacs.forEach(uacs => {
                const item = document.createElement('div');
                item.className = 'search-select-option';
                item.textContent = uacs;
                item.addEventListener('click', function() {
                    searchUacs.value = uacs;
                    document.getElementById('selectedUacs').value = uacs;
                    uacsDropdown.style.display = 'none';
                    // 当选择账户后，检查SKU重复性
                    const sku = document.getElementById('addSku').value.trim();
                    if (sku) {
                        checkSkuDuplicate(sku, uacs);
                    }
                });
                uacsDropdown.appendChild(item);
            });
            uacsDropdown.style.display = 'block';
        } else {
            const noResult = document.createElement('div');
            noResult.className = 'search-select-no-results';
            noResult.textContent = '未找到匹配的账户';
            uacsDropdown.appendChild(noResult);
            uacsDropdown.style.display = 'block';
        }
    });
    
    // 点击页面其他地方隐藏下拉菜单
    document.addEventListener('click', function(event) {
        if (!searchUacs.contains(event.target) && !uacsDropdown.contains(event.target)) {
            uacsDropdown.style.display = 'none';
        }
    });
    // ============== 新增功能相关代码结束 ==============
});

// 全选
document.getElementById('selectAll').addEventListener('change', function() {
    document.querySelectorAll('.row-checkbox').forEach(cb => cb.checked = this.checked);
});

// ============== 批量新增功能相关代码 ==============
let isBatchMode = false;
let uacsPrefixList = []; // 存储账户前缀列表

// 切换批量新增模式
document.getElementById('toggleBatchMode').addEventListener('click', function() {
    isBatchMode = !isBatchMode;
    
    if (isBatchMode) {
        // 切换到批量模式
        this.textContent = '切换到单条新增模式';
        this.style.background = '#67c23a';
        document.getElementById('singleUacsField').style.display = 'none';
        document.getElementById('batchUacsField').style.display = 'block';
        document.getElementById('submitAdd').textContent = '批量添加记录';
        
        // 加载账户前缀列表
        loadUacsPrefixList();
        document.getElementById('searchUacsPrefix').focus();
    } else {
        // 切换回单条模式
        this.textContent = '切换到批量新增模式';
        this.style.background = '#e6a23c';
        document.getElementById('singleUacsField').style.display = 'block';
        document.getElementById('batchUacsField').style.display = 'none';
        document.getElementById('submitAdd').textContent = '添加记录';
        
        // 隐藏预览
        document.getElementById('batchUacsPreview').style.display = 'none';
        // 清除验证状态
        clearSellerValidation();
        clearSkuValidation();
    }
});

// 加载账户前缀列表
function loadUacsPrefixList() {
    fetch('GetUacsPrefixListServlet')
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                uacsPrefixList = data.prefixList;
            } else {
                showToast('❌ 获取账户前缀列表失败', true);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('❌ 获取账户前缀列表时出错', true);
        });
}

// 账户前缀搜索功能
const searchUacsPrefix = document.getElementById('searchUacsPrefix');
const uacsPrefixDropdown = document.getElementById('uacsPrefixDropdown');

searchUacsPrefix.addEventListener('input', function() {
    const searchTerm = this.value.toLowerCase();
    
    if (searchTerm.length === 0) {
        uacsPrefixDropdown.style.display = 'none';
        document.getElementById('batchUacsPreview').style.display = 'none';
        return;
    }
    
    // 过滤账户前缀列表
    const filteredPrefixes = uacsPrefixList.filter(prefix => 
        prefix.toLowerCase().includes(searchTerm)
    );
    
    // 显示下拉菜单
    uacsPrefixDropdown.innerHTML = '';
    if (filteredPrefixes.length > 0) {
        filteredPrefixes.forEach(prefix => {
            const item = document.createElement('div');
            item.className = 'search-select-option';
            item.textContent = prefix;
            item.addEventListener('click', function() {
                searchUacsPrefix.value = prefix;
                document.getElementById('selectedUacsPrefix').value = prefix;
                uacsPrefixDropdown.style.display = 'none';
                
                // 显示预览
                showUacsPreview(prefix);
                // 检查批量SKU重复性
                const sku = document.getElementById('addSku').value.trim();
                if (sku && prefix) {
                    checkBatchSkuDuplicate(sku, prefix);
                }
            });
            uacsPrefixDropdown.appendChild(item);
        });
        uacsPrefixDropdown.style.display = 'block';
    } else {
        const noResult = document.createElement('div');
        noResult.className = 'search-select-no-results';
        noResult.textContent = '未找到匹配的账户前缀';
        uacsPrefixDropdown.appendChild(noResult);
        uacsPrefixDropdown.style.display = 'block';
        document.getElementById('batchUacsPreview').style.display = 'none';
    }
});

// 显示账户预览
function showUacsPreview(prefix) {
    fetch('GetUacsByPrefixServlet?prefix=' + encodeURIComponent(prefix))
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                const uacsList = data.uacsList;
                const preview = document.getElementById('previewUacsList');
                preview.innerHTML = uacsList.join(', ');
                document.getElementById('batchUacsPreview').style.display = 'block';
            } else {
                showToast('❌ 获取账户列表失败', true);
            }
        })
        .catch(error => {
            console.error('Error:', error);
            showToast('❌ 获取账户列表时出错', true);
        });
}

// 点击页面其他地方隐藏下拉菜单
document.addEventListener('click', function(event) {
    if (!searchUacsPrefix.contains(event.target) && !uacsPrefixDropdown.contains(event.target)) {
        uacsPrefixDropdown.style.display = 'none';
    }
});

// ============== Seller后端验证功能 ==============
function validateSellerWithBackend(sellerName) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open('POST', 'ValidateSellerServlet', true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    try {
                        const res = JSON.parse(xhr.responseText);
                        resolve({
                            isValid: res.success,
                            message: res.message || ''
                        });
                    } catch (e) {
                        reject(new Error('响应解析失败'));
                    }
                } else {
                    reject(new Error('请求失败: ' + xhr.status));
                }
            }
        };
        
        xhr.send(JSON.stringify({ seller: sellerName }));
    });
}

// ============== SKU重复性验证功能 ==============
function checkSkuDuplicate(sku, uacs) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open('POST', 'CheckSkuDuplicateServlet', true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    try {
                        const res = JSON.parse(xhr.responseText);
                        resolve(res);
                    } catch (e) {
                        reject(new Error('响应解析失败'));
                    }
                } else {
                    reject(new Error('请求失败: ' + xhr.status));
                }
            }
        };
        
        xhr.send(JSON.stringify({ sku: sku, uacs: uacs }));
    });
}

// ============== 批量SKU重复性验证功能 ==============
function checkBatchSkuDuplicate(sku, prefix) {
    return new Promise((resolve, reject) => {
        const xhr = new XMLHttpRequest();
        xhr.open('POST', 'CheckBatchSkuDuplicateServlet', true);
        xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {
                if (xhr.status === 200) {
                    try {
                        const res = JSON.parse(xhr.responseText);
                        resolve(res);
                    } catch (e) {
                        reject(new Error('响应解析失败'));
                    }
                } else {
                    reject(new Error('请求失败: ' + xhr.status));
                }
            }
        };
        
        xhr.send(JSON.stringify({ sku: sku, prefix: prefix }));
    });
}

// 清除Seller验证状态
function clearSellerValidation() {
    const sellerInput = document.getElementById('addSeller');
    sellerInput.classList.remove('seller-valid', 'seller-invalid');
    const hint = document.getElementById('sellerValidationHint');
    if (hint) {
        hint.innerHTML = '';
        hint.style.display = 'none';
    }
}

// 清除SKU验证状态
function clearSkuValidation() {
    const skuInput = document.getElementById('addSku');
    skuInput.classList.remove('sku-valid', 'sku-invalid');
    const hint = document.getElementById('skuValidationHint');
    if (hint) {
        hint.innerHTML = '';
        hint.style.display = 'none';
    }
}

// 显示Seller验证提示
function showSellerHint(message, isError) {
    const hint = document.getElementById('sellerValidationHint');
    if (!hint) return;
    
    hint.innerHTML = message;
    hint.style.cssText = `
        display: block;
        font-size: 12px;
        margin-top: 4px;
        padding: 4px 8px;
        border-radius: 4px;
        background: ${isError ? '#fef0f0' : '#f0f9eb'};
        color: ${isError ? '#f56c6c' : '#67c23a'};
        border: 1px solid ${isError ? '#fbc4c4' : '#e1f3d8'};
    `;
}

// 显示SKU验证提示
function showSkuHint(htmlMessage, isError) {
    const hint = document.getElementById('skuValidationHint');
    if (!hint) return;
    
    hint.innerHTML = htmlMessage; // ✅ 关键：支持HTML
    
    if (htmlMessage.includes('⚠️')) {
        hint.style.cssText = `
            display: block;
            font-size: 12px;
            margin-top: 4px;
            padding: 6px 8px;
            border-radius: 4px;
            background: #fdf6ec;
            color: #e6a23c;
            border: 1px solid #faecd8;
            line-height: 1.4;
        `;
    } else {
        hint.style.cssText = `
            display: block;
            font-size: 12px;
            margin-top: 4px;
            padding: 6px 8px;
            border-radius: 4px;
            background: ${isError ? '#fef0f0' : '#f0f9eb'};
            color: ${isError ? '#f56c6c' : '#67c23a'};
            border: 1px solid ${isError ? '#fbc4c4' : '#e1f3d8'};
            line-height: 1.4;
        `;
    }
}

// 为Seller输入框添加实时验证
document.getElementById('addSeller').addEventListener('blur', function() {
    const seller = this.value.trim();
    if (seller) {
        validateSellerWithBackend(seller).then(result => {
            if (result.isValid) {
                // 验证通过
                this.classList.remove('seller-invalid');
                this.classList.add('seller-valid');
                showSellerHint('✅ Seller有效', false);
            } else {
                // 验证失败
                this.classList.remove('seller-valid');
                this.classList.add('seller-invalid');
                showSellerHint('❌ ' + result.message, true);
            }
        }).catch(error => {
            console.error('Seller验证失败:', error);
            // 验证服务异常时不改变样式，只显示提示
            showSellerHint('⚠️ 验证服务异常，请稍后重试', true);
        });
    } else {
        // 输入为空时清除验证状态
        clearSellerValidation();
    }
});

// 为SKU输入框添加实时验证（单条模式）
document.getElementById('addSku').addEventListener('blur', function() {
    const sku = this.value.trim();
    const uacs = document.getElementById('selectedUacs').value;
    
    if (sku && uacs && !isBatchMode) {
        // ========== 单条新增模式：严格校验 ==========
        checkSkuDuplicate(sku, uacs).then(result => {
            if (result.exists) {
                // SKU已存在 → 标红 + 错误提示
                this.classList.remove('sku-valid');
                this.classList.add('sku-invalid');
                showSkuHint('❌ 该SKU在此账户中已存在', true);
            } else {
                // SKU可用 → 标绿 + 成功提示
                this.classList.remove('sku-invalid');
                this.classList.add('sku-valid');
                showSkuHint('✅ SKU可用', false);
            }
        }).catch(error => {
            console.error('SKU重复性检查失败:', error);
            this.classList.remove('sku-valid', 'sku-invalid');
            showSkuHint('⚠️ 重复性检查服务异常', true);
        });
        
    } else if (sku && isBatchMode) {
        // ========== 批量新增模式：仅提示，不拦截 ==========
        const prefix = document.getElementById('selectedUacsPrefix').value;
        if (prefix) {
            checkBatchSkuDuplicate(sku, prefix).then(result => {
                if (!result.success) {
                    // 后端返回错误
                    this.classList.remove('sku-valid', 'sku-invalid');
                    showSkuHint('⚠️ ' + result.message, true);
                    return;
                }

                const duplicateList = result.duplicateUacs || [];
                const existsCount = duplicateList.length;
                if (existsCount > 0) {
                    const accountList = duplicateList.join(', ');
                    const message = 
                        '⚠️ 该 SKU 在以下 <strong>' + existsCount + '</strong> 个账户中已存在，将自动跳过：<br>' +
                        '<strong style="color:#e6a23c;">' + accountList + '</strong>';
                    
                    this.classList.remove('sku-invalid');
                    this.classList.add('sku-valid');
                    showSkuHint(message, false);
                } else {
                    this.classList.remove('sku-invalid');
                    this.classList.add('sku-valid');
                    showSkuHint('✅ SKU在所有账户中均可用', false);
                }
            }).catch(error => {
                console.error('批量SKU重复性检查失败:', error);
                this.classList.remove('sku-valid', 'sku-invalid');
                showSkuHint('⚠️ 重复性检查服务异常，但仍可提交', true);
            });
        } else {
            // 未选择前缀
            clearSkuValidation();
        }
    } else {
        // SKU为空 或 未选择账户/前缀
        clearSkuValidation();
    }
});

// 批量新增提交函数
function submitBatchAddRequest(formData) {
    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'BatchAddAmazonDataServlet', true);
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                try {
                    const res = JSON.parse(xhr.responseText);
                    if (res.success) {
                        showToast('✅ 批量新增成功！共新增 ' + res.count + ' 条记录');
                        document.getElementById('addModal').style.display = 'none';
                        // 刷新页面以显示新数据
                        setTimeout(() => window.location.reload(), 1500);
                    } else {
                        showToast('❌ ' + res.message, true);
                    }
                } catch (e) {
                    showToast('❌ 数据解析错误', true);
                }
            } else {
                showToast('❌ 请求失败（' + xhr.status + '）', true);
            }
        }
    };
    xhr.send(JSON.stringify(formData));
}

// 修改提交事件处理，支持批量新增和Seller验证
document.getElementById('addForm').addEventListener('submit', function(e) {
    e.preventDefault();
    
    if (isBatchMode) {
        // 批量新增逻辑
        const prefix = document.getElementById('selectedUacsPrefix').value;
        const sku = document.getElementById('addSku').value.trim();
        const seller = document.getElementById('addSeller').value.trim();
        
        if (!prefix) {
            showToast('❌ 请选择账户前缀', true);
            return;
        }
        if (!sku) {
            showToast('❌ SKU不能为空', true);
            return;
        }
        if (!seller) {
            showToast('❌ Seller不能为空', true);
            return;
        }
        
        // ============ Seller 后端验证 ============
        validateSellerWithBackend(seller).then(validationResult => {
            if (validationResult.isValid) {
                // ✅ 不再检查 SKU 重复性！直接提交
                const formData = {
                    prefix: prefix,
                    sku: sku,
                    seller: seller,
                    warehouseSku: document.getElementById('addWarehouseSku').value.trim(),
                    asin: document.getElementById('addAsin').value.trim(),
                    parentAsin: document.getElementById('addParentAsin').value.trim(),
                    lifecl: document.getElementById('addLifecl').value,
                    batchMode: true
                };
                
                submitBatchAddRequest(formData);
            } else {
                showToast('❌ ' + validationResult.message, true);
                document.getElementById('addSeller').classList.add('seller-invalid');
                document.getElementById('addSeller').focus();
            }
        }).catch(error => {
            console.error('Seller验证失败:', error);
            showToast('❌ Seller验证服务异常，请稍后重试', true);
        });
    }
        
     else {
        // 原有的单条新增逻辑
        const uacs = document.getElementById('selectedUacs').value;
        const sku = document.getElementById('addSku').value.trim();
        const seller = document.getElementById('addSeller').value.trim();
        
        if (!uacs) {
            showToast('❌ 请选择账户', true);
            return;
        }
        if (!sku) {
            showToast('❌ SKU不能为空', true);
            return;
        }
        if (!seller) {
            showToast('❌ Seller不能为空', true);
            return;
        }
        
        // ============ 新增：单条SKU重复性检查 ============
        checkSkuDuplicate(sku, uacs).then(skuResult => {
            if (skuResult.exists) {
                showToast('❌ 该SKU在此账户中已存在，无法新增', true);
                document.getElementById('addSku').classList.add('sku-invalid');
                document.getElementById('addSku').focus();
                return;
            }
            
            // 构建请求数据
            const formData = {
                uacs: uacs,
                sku: sku,
                seller: seller,
                warehouseSku: document.getElementById('addWarehouseSku').value.trim(),
                asin: document.getElementById('addAsin').value.trim(),
                parentAsin: document.getElementById('addParentAsin').value.trim(),
                lifecl: document.getElementById('addLifecl').value
            };
            
            // 发送AJAX请求
            const xhr = new XMLHttpRequest();
            xhr.open('POST', 'AddAmazonDataServlet', true);
            xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4) {
                    if (xhr.status === 200) {
                        try {
                            const res = JSON.parse(xhr.responseText);
                            if (res.success) {
                                showToast('✅ 新增成功！');
                                document.getElementById('addModal').style.display = 'none';
                                // 刷新页面以显示新数据
                                setTimeout(() => window.location.reload(), 1000);
                            } else {
                                showToast('❌ ' + res.message, true);
                            }
                        } catch (e) {
                            showToast('❌ 数据解析错误', true);
                        }
                    } else {
                        showToast('❌ 请求失败（' + xhr.status + '）', true);
                    }
                }
            };
            xhr.send(JSON.stringify(formData));
        }).catch(error => {
            console.error('SKU重复性检查失败:', error);
            showToast('❌ SKU重复性检查服务异常，请稍后重试', true);
        });
    }
});

// 修改新增按钮点击事件，重置批量模式状态
document.getElementById('addNewBtn').addEventListener('click', function() {
    // 重置表单和状态
    document.getElementById('addForm').reset();
    document.getElementById('searchUacs').value = '';
    document.getElementById('selectedUacs').value = '';
    document.getElementById('searchUacsPrefix').value = '';
    document.getElementById('selectedUacsPrefix').value = '';
    document.getElementById('uacsDropdown').style.display = 'none';
    document.getElementById('uacsPrefixDropdown').style.display = 'none';
    document.getElementById('batchUacsPreview').style.display = 'none';
    
    // 清除验证状态
    clearSellerValidation();
    clearSkuValidation();
    
    // 重置为单条模式
    if (isBatchMode) {
        document.getElementById('toggleBatchMode').click(); // 触发切换
    }
    
    document.getElementById('addModal').style.display = 'flex';
    document.getElementById('searchUacs').focus();
});

// 批量按钮
document.getElementById('batchEditBtn').addEventListener('click', function() {
    const selected = document.querySelectorAll('.row-checkbox:checked');
    if (selected.length === 0) {
        showToast('❌ 请至少选择一条记录', true);
        return;
    }
    document.getElementById('selectedCount').textContent = selected.length;
    document.getElementById('batchEditModal').style.display = 'flex';
});

// 批量模态框交互
document.getElementById('cancelBatchEdit').addEventListener('click', function() {
    document.getElementById('batchEditModal').style.display = 'none';
    document.getElementById('updateSeller').checked = false;
    document.getElementById('updateLifecl').checked = false;
    document.getElementById('batchSeller').style.display = 'none';
    document.getElementById('batchLifecl').style.display = 'none';
});

// 新增模态框交互
document.getElementById('cancelAdd').addEventListener('click', function() {
    document.getElementById('addModal').style.display = 'none';
});

document.getElementById('updateSeller').addEventListener('change', function() {
    document.getElementById('batchSeller').style.display = this.checked ? 'block' : 'none';
});
document.getElementById('updateLifecl').addEventListener('change', function() {
    document.getElementById('batchLifecl').style.display = this.checked ? 'block' : 'none';
});

// 提交批量修改
document.getElementById('batchEditForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const selected = document.querySelectorAll('.row-checkbox:checked');
    const ids = Array.from(selected).map(cb => parseInt(cb.dataset.id));

    let seller = null;
    let lifecl = null;

    if (document.getElementById('updateSeller').checked) {
        seller = document.getElementById('batchSeller').value.trim();
        if (seller === '') {
            showToast('❌ Seller 不能为空', true);
            return;
        }
    }
    if (document.getElementById('updateLifecl').checked) {
        lifecl = document.getElementById('batchLifecl').value;
        if (lifecl === '') {
            showToast('❌ 请选择 Lifecl 状态', true);
            return;
        }
    }
    if (seller === null && lifecl === null) {
        showToast('❌ 请至少选择一个字段进行修改', true);
        return;
    }

    const xhr = new XMLHttpRequest();
    xhr.open('POST', 'BatchUpdateAmazonDataServlet', true);
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                try {
                    const res = JSON.parse(xhr.responseText);
                    if (res.success) {
                        showToast('✅ ' + res.message);
                        setTimeout(() => window.location.reload(), 1000);
                    } else {
                        showToast('❌ ' + res.message, true);
                    }
                } catch (e) {
                    showToast('❌ 数据解析错误', true);
                }
            } else {
                showToast('❌ 请求失败（' + xhr.status + '）', true);
            }
        }
    };
    xhr.send(JSON.stringify({ ids: ids, seller: seller, lifecl: lifecl }));
});

// 单条修改（原有逻辑）
var editButtons = document.querySelectorAll('.edit-btn');
for (var i = 0; i < editButtons.length; i++) {
    editButtons[i].addEventListener('click', function() {
        var id = this.getAttribute('data-id');
        var sku = this.getAttribute('data-sku');
        var uacs = this.getAttribute('data-uacs');
        var seller = this.getAttribute('data-seller');
        var lifecl = this.getAttribute('data-lifecl');

        document.getElementById('editId').value = id;
        document.getElementById('editIdDisplay').value = id;
        document.getElementById('editSku').value = sku;
        document.getElementById('editUacs').value = uacs;
        document.getElementById('editSeller').value = seller;
        document.getElementById('editSeller').setAttribute('data-original-seller', seller || '');
        
        var lifeclSelect = document.getElementById('editLifecl');
        lifeclSelect.value = lifecl || '';

        document.getElementById('editModal').style.display = 'flex';
    });
}

document.getElementById('cancelEdit').addEventListener('click', function() {
    document.getElementById('editModal').style.display = 'none';
});

document.getElementById('editForm').addEventListener('submit', function(e) {
    e.preventDefault();
    var sellerInput = document.getElementById('editSeller');
    var originalSeller = sellerInput.getAttribute('data-original-seller');
    var currentSeller = sellerInput.value.trim();

    if (originalSeller !== "" && originalSeller !== "null" && originalSeller !== "-") {
        if (currentSeller === "") {
            showToast('❌ 不能清空已分配的 Seller', true);
            sellerInput.focus();
            return;
        }
    }

    var formData = {
        id: document.getElementById('editId').value,
        seller: currentSeller,
        lifecl: document.getElementById('editLifecl').value
    };

    var xhr = new XMLHttpRequest();
    xhr.open('POST', 'UpdateAmazonDataServlet', true);
    xhr.setRequestHeader('Content-Type', 'application/json; charset=UTF-8');
    xhr.onreadystatechange = function() {
        if (xhr.readyState === 4) {
            if (xhr.status === 200) {
                try {
                    var data = JSON.parse(xhr.responseText);
                    if (data.success) {
                        showToast('✅ 修改成功！');
                        var row = document.querySelector('tr[data-id="' + formData.id + '"]');
                        if (row) {
                            row.cells[5].textContent = formData.seller || '-';
                            row.cells[9].textContent = formData.lifecl || '-';
                        }
                        document.getElementById('editModal').style.display = 'none';
                    } else {
                        showToast('❌ ' + data.message, true);
                    }
                } catch (e) {
                    showToast('❌ 数据解析错误', true);
                }
            } else {
                showToast('❌ 请求失败（状态码：' + xhr.status + '）', true);
            }
        }
    };
    xhr.send(JSON.stringify(formData));
});

// 跳转
function jumpToPage(size, commonParams) {
    var input = document.getElementById('jumpPage');
    var page = parseInt(input.value);
    var totalPages = <%=totalPages%>;
    if (isNaN(page) || page < 1 || page > totalPages) {
        showToast('请输入 1 到 ' + totalPages + ' 之间的有效页码', true);
        input.focus();
        return;
    }
    window.location.href = 'ListAmazonDataServlet?page=' + page + '&size=' + size + commonParams;   
}

document.getElementById('jumpPage').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') {
        jumpToPage(<%=pageSize%>, '<%=commonParams%>');
    }
});
</script>
</body>
</html>