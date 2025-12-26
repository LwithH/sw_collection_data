<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.model.CollectionData" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>福来广告数据表 - 列表</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .user-info { text-align: right; margin-bottom: 20px; color: #666; }
        .user-info a { color: #f56c6c; text-decoration: none; margin-left: 10px; }
        h1 { color: #333; margin-bottom: 25px; padding-bottom: 10px; border-bottom: 2px solid #67c23a; }
        .data-table { width: 100%; border-collapse: collapse; margin-bottom: 20px; }
        .data-table th, .data-table td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
        .data-table th { background-color: #f8f9fa; font-weight: bold; color: #555; }
        .data-table tr:hover { background-color: #f9f9f9; }

        /* 广告标题列：限制宽度 + 最多2行 + 超出省略 */
        .campaign-name {
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
            max-width: 250px;
            line-height: 1.4;
            word-break: break-word;
        }

        /* 操作列按钮样式 */
        .action-btn {
            padding: 4px 8px;
            font-size: 12px;
            background: #67c23a;
        }
        .action-btn:hover {
            background: #5ab735;
        }

        .pagination { text-align: center; margin: 20px 0; }
        .pagination a, .pagination span { 
            display: inline-block; 
            padding: 6px 12px; 
            margin: 0 3px; 
            border: 1px solid #ddd; 
            border-radius: 4px; 
            text-decoration: none; 
            color: #409eff; 
        }
        .pagination .current { 
            background-color: #409eff; 
            color: white; 
            border-color: #409eff; 
        }
        .pagination a:hover { background-color: #f0f5ff; }
        .actions { white-space: nowrap; }
        .btn { padding: 6px 10px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 14px; text-decoration: none; display: inline-block; }
        .btn:hover { background: #3390e0; }
        .page-size-form { text-align: center; margin-bottom: 15px; }
        .page-size-form select { padding: 6px; border-radius: 4px; border: 1px solid #ddd; }
        .back-link { display: inline-block; margin-bottom: 20px; color: #67c23a; text-decoration: none; }
        .back-link:hover { text-decoration: underline; }
        
        /* 搜索和筛选区域样式 */
        .search-filter-container {
            display: flex;
            gap: 15px;
            margin-bottom: 20px;
            align-items: center;
            flex-wrap: wrap;
            background: #f8f9fa;
            padding: 15px;
            border-radius: 6px;
        }
        .search-box {
            flex: 1;
            min-width: 250px;
            max-width: 400px;
            position: relative;
        }
        .search-box input {
            width: 100%;
            padding: 10px 15px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
        }
        .search-box button {
            position: absolute;
            right: 5px;
            top: 5px;
            padding: 3px 8px;
            font-size: 12px;
        }
        .filter-option {
            display: flex;
            align-items: center;
            gap: 8px;
            padding: 5px 10px;
            background: white;
            border-radius: 4px;
            border: 1px solid #eee;
        }
        .filter-option input[type="checkbox"] {
            width: 16px;
            height: 16px;
            margin: 0;
            cursor: pointer;
        }
        .filter-option label {
            margin: 0;
            cursor: pointer;
            font-weight: normal;
            color: #333;
        }
        .actions-container {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 15px;
        }
        .filter-need-edit {
            margin-left: 20px; /* 向右移动20px */
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- 返回首页 + 用户信息（可选） -->
        <a href="index.jsp" class="back-link">⬅ 返回首页</a>
        
        <!-- 搜索和筛选区域 -->
        <div class="actions-container">
            <div class="search-filter-container">
                <div class="search-box">
                    <%
                        // 获取搜索关键词
                        String searchKeyword = request.getParameter("searchKeyword");
                    %>
                    <input type="text" id="searchKeyword" 
                           placeholder="搜索账号、广告标题、金额..." 
                           value="<%= searchKeyword != null ? searchKeyword : "" %>">
                    <button onclick="searchData()" class="btn">搜索</button>
                </div>
                
                <!-- 筛选需要编辑的复选框 -->
                <div class="filter-option filter-need-edit">
                    <%
                        // 获取筛选参数
                        String needEdit = request.getParameter("needEdit");
                        boolean isNeedEditChecked = "true".equals(needEdit);
                    %>
                    <input type="checkbox" id="needEdit" 
                           <%= isNeedEditChecked ? "checked" : "" %>
                           onclick="toggleNeedEdit()">
                    <label for="needEdit">筛选需要编辑 </label>
                </div>
                
                <div>
                    <button onclick="resetFilters()" class="btn" style="background: #e6a23c;">重置</button>
                </div>
            </div>
        </div>

        <h1> 福来广告 - 列表</h1>

        <!-- 每页显示条数选择 -->
        <form class="page-size-form" action="ListNewTableServlet" method="get">
            <%
                // 获取每页条数
                String pageSizeStr = request.getParameter("pageSize");
                int pageSize = 20;
                try {
                    if (pageSizeStr != null && !pageSizeStr.trim().isEmpty()) {
                        pageSize = Integer.parseInt(pageSizeStr);
                        if (pageSize <= 0) pageSize = 20;
                        if (pageSize > 500) pageSize = 500;
                    }
                } catch (NumberFormatException e) {
                    pageSize = 20;
                }
            %>
            每页显示
            <select name="pageSize" onchange="this.form.submit()">
                <option value="10" <%= pageSize == 10 ? "selected" : "" %>>10 条</option>
                <option value="20" <%= pageSize == 20 ? "selected" : "" %>>20 条</option>
                <option value="50" <%= pageSize == 50 ? "selected" : "" %>>50 条</option>
                <option value="100" <%= pageSize == 100 ? "selected" : "" %>>100 条</option>
            </select>
            
            <!-- 保留搜索和筛选参数 -->
            <% if (searchKeyword != null && !searchKeyword.isEmpty()) { %>
                <input type="hidden" name="searchKeyword" value="<%= searchKeyword %>">
            <% } %>
            <% if (isNeedEditChecked) { %>
                <input type="hidden" name="needEdit" value="true">
            <% } %>
            
            ，共 <strong><%= request.getAttribute("totalCount") != null ? request.getAttribute("totalCount") : "0" %></strong> 条记录
        </form>

        <!-- 数据表格 -->
        <table class="data-table">
            <thead>
                <tr>
                    <th>ID</th>
                    <th>账号</th>
                    <th>广告标题</th>
                    <th>金额</th>
                    <th>币种</th>
                    <th>仓库sku</th>
                    <th>spu</th>
                    <th>部门</th>
                    <th style="width: 80px; text-align: center;">操作</th>
                </tr>
            </thead>
            <tbody>
                <%
                    // 获取页码参数
                    Integer currentPage = (Integer) request.getAttribute("currentPage");
                    if (currentPage == null) currentPage = 1;

                    List<CollectionData> dataList = (List<CollectionData>) request.getAttribute("dataList");
                    if (dataList != null && !dataList.isEmpty()) {
                        for (CollectionData data : dataList) {
                %>
                <tr>
                    <td><%= data.getId() %></td>
                    <td><%= data.getAccount() != null ? data.getAccount() : "-" %></td>
                    <td class="campaign-name" title="<%= data.getCampaignName() != null ? data.getCampaignName() : "-" %>">
                        <%= data.getCampaignName() != null ? data.getCampaignName() : "-" %>
                    </td>
                    <td><%= data.getAmount() != null ? data.getAmount() : "0.00" %></td>
                    <td><%= data.getCurrency() != null ? data.getCurrency() : "-" %></td>
                    <td><%= data.getWarehouseSku() != null ? data.getWarehouseSku() : "-" %></td>
                    <td><%= data.getSpu() != null ? data.getSpu() : "-" %></td>
                    <td>
                        <%
                            // 部门代码映射 - 处理String类型的salesDepart
                            String salesDepart = data.getSalesDepart();
                            String departName = "-";
                            if (salesDepart != null && !salesDepart.isEmpty()) {
                                try {
                                    int departCode = Integer.parseInt(salesDepart);
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
                                        default: departName = "未知部门"; break;
                                    }
                                } catch (NumberFormatException e) {
                                    // 如果转换失败，显示原始值
                                    departName = "未知部门";
                                }
                            }
                            out.print(departName);
                        %>
                    </td>
                    <td style="text-align: center;">
                        <%
                            if (data.getCreateUserId() == 1) {
                        %>
                            <a href="EditNewTableServlet?id=<%= data.getId() %>&page=<%= currentPage %>&pageSize=<%= pageSize %><%= searchKeyword != null ? "&searchKeyword=" + java.net.URLEncoder.encode(searchKeyword, "UTF-8") : "" %><%= isNeedEditChecked ? "&needEdit=true" : "" %>" 
                               class="btn action-btn">修改</a>
                        <%
                            } else {
                        %>
                            <span style="color: #ccc; font-size: 12px;">无需修改</span>
                        <%
                            }
                        %>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="9" style="text-align: center; color: #999;">暂无数据</td>
                </tr>
                <%
                    }
                %>
            </tbody>
        </table>

      <!-- ========== 分页导航（增加首页和末页） ========== -->
<div class="pagination">
    <%
        // 获取总页数
        Integer totalPages = (Integer) request.getAttribute("totalPages");
        if (totalPages == null) totalPages = 1;
        
        // 构建URL参数（复用原有逻辑）
        StringBuilder urlParams = new StringBuilder();
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            urlParams.append("&searchKeyword=").append(java.net.URLEncoder.encode(searchKeyword, "UTF-8"));
        }
        if (isNeedEditChecked) {
            urlParams.append("&needEdit=true");
        }
        urlParams.append("&pageSize=").append(pageSize);
    %>

    <!-- 首页按钮 -->
    <% if (currentPage > 1) { %>
        <a href="ListNewTableServlet?page=1<%= urlParams.toString() %>">首页</a>
    <% } else { %>
        <span style="color: #ccc;">首页</span>
    <% } %>

    <!-- 上一页（原有） -->
    <% if (currentPage > 1) { %>
        <a href="ListNewTableServlet?page=<%= currentPage - 1 %><%= urlParams.toString() %>">&laquo; 上一页</a>
    <% } else { %>
        <span style="color: #ccc;">&laquo; 上一页</span>
    <% } %>

    <!-- 页码（原有） -->
    <%
        int startPage = Math.max(1, currentPage - 2);
        int endPage = Math.min(totalPages, currentPage + 2);
        
        for (int i = startPage; i <= endPage; i++) {
            if (i == currentPage) {
    %>
        <span class="current"><%= i %></span>
    <% } else { %>
        <a href="ListNewTableServlet?page=<%= i %><%= urlParams.toString() %>"><%= i %></a>
    <% } } %>

    <!-- 下一页（原有） -->
    <% if (currentPage < totalPages) { %>
        <a href="ListNewTableServlet?page=<%= currentPage + 1 %><%= urlParams.toString() %>">下一页 &raquo;</a>
    <% } else { %>
        <span style="color: #ccc;">下一页 &raquo;</span>
    <% } %>

    <!-- 末页按钮 -->
    <% if (currentPage < totalPages) { %>
        <a href="ListNewTableServlet?page=<%= totalPages %><%= urlParams.toString() %>">末页</a>
    <% } else { %>
        <span style="color: #ccc;">末页</span>
    <% } %>
</div>
        
        <!-- 页码跳转提示 -->
        <div style="text-align: center; color: #999; font-size: 14px;">
            第 <%= currentPage %> 页 / 共 <%= totalPages %> 页
        </div>
    </div>

    <!-- 搜索和筛选脚本 -->
    <script>
        // 搜索功能
        function searchData() {
            const searchKeyword = document.getElementById('searchKeyword').value.trim();
            const needEdit = document.getElementById('needEdit').checked;
            let url = 'ListNewTableServlet?page=1';
            
            if (searchKeyword) {
                url += '&searchKeyword=' + encodeURIComponent(searchKeyword);
            }
            
            if (needEdit) {
                url += '&needEdit=true';
            }
            
            // 添加pageSize参数
            const pageSizeSelect = document.querySelector('.page-size-form select');
            if (pageSizeSelect) {
                url += '&pageSize=' + pageSizeSelect.value;
            }
            
            window.location.href = url;
        }
        
        // 筛选切换（复选框点击）
        function toggleNeedEdit() {
            searchData(); // 复选框状态改变时直接触发搜索
        }
        
        // 重置过滤器
        function resetFilters() {
            document.getElementById('searchKeyword').value = '';
            document.getElementById('needEdit').checked = false;
            
            // 获取当前的pageSize
            const pageSizeSelect = document.querySelector('.page-size-form select');
            const pageSize = pageSizeSelect ? pageSizeSelect.value : '20';
            
            window.location.href = 'ListNewTableServlet?pageSize=' + pageSize;
        }
        
        // 按下回车键触发搜索
        document.getElementById('searchKeyword').addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                searchData();
            }
        });
    </script>
</body>
</html>