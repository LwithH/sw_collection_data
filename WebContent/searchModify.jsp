<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.model.CollectionData" %>
<%@ page import="com.model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>搜索和修改数据</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .user-info { text-align: right; margin-bottom: 20px; color: #666; }
        .user-info a { color: #f56c6c; text-decoration: none; margin-left: 10px; }
        h1 { color: #333; margin-bottom: 25px; padding-bottom: 10px; border-bottom: 2px solid #409eff; }
        .search-form { margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 4px; }
        .search-form input[type="text"] { padding: 8px 12px; width: 300px; border: 1px solid #ddd; border-radius: 4px; }
        .search-form button { padding: 8px 15px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer; }
        .btn { display: inline-block; padding: 5px 10px; background: #409eff; color: white; text-decoration: none; border-radius: 4px; margin-right: 5px; }
        .btn-edit { background: #67c23a; }
        .btn:hover { opacity: 0.9; }
        table { width: 100%; border-collapse: collapse; }
        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #f8f9fa; color: #666; font-weight: bold; }
        tr:hover { background: #f5f5f5; }
        .no-data { text-align: center; padding: 40px; color: #999; }
        .pagination { margin-top: 20px; text-align: center; }
        .pagination a { display: inline-block; padding: 5px 12px; margin: 0 3px; border: 1px solid #ddd; border-radius: 4px; text-decoration: none; color: #409eff; }
        .pagination a.active { background: #409eff; color: white; border-color: #409eff; }
        .pagination select { margin-right: 15px; }
        .msg { padding: 10px; margin-bottom: 20px; border-radius: 4px; }
        .success { background: #f0f9eb; color: #67c23a; }
        .error { background: #fef0f0; color: #f56c6c; }
        .link { margin-top: 20px; }

        /* 新增：长文本列防撑破 */
        .break-word {
            word-break: break-all;
            max-width: 180px;
            line-height: 1.4;
        }

        /* 新增：页码输入框和按钮样式 */
        .pagination .page-input-group {
            display: inline-block;
            margin-left: 20px;
        }
        .pagination .page-input {
            width: 60px;
            padding: 5px 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
            font-size: 14px;
            text-align: center;
            margin: 0 5px;
            outline: none;
        }
        .pagination .page-input:focus {
            border-color: #409eff;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
        }
        .pagination .page-btn {
            padding: 5px 12px;
            border: 1px solid #409eff;
            border-radius: 4px;
            background: white;
            color: #409eff;
            font-size: 14px;
            cursor: pointer;
            transition: all 0.2s;
        }
        .pagination .page-btn:hover {
            background-color: #f5faff;
            border-color: #66b1ff;
        }
        .pagination .page-btn:active {
            background-color: #e6f7ff;
        }

        /* 新增：提示框样式 */
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 15px;
            background: white;
            border-radius: 4px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.1);
            z-index: 1000;
            transform: translateX(400px);
            opacity: 0;
            transition: all 0.3s ease;
            max-width: 300px;
        }
        .notification.show {
            transform: translateX(0);
            opacity: 1;
        }
        .notification.error {
            border-left: 4px solid #f56c6c;
        }
        .notification .title {
            font-weight: bold;
            margin-bottom: 5px;
            color: #333;
        }
        .notification .message {
            color: #666;
            font-size: 14px;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- 登录用户信息 -->
        <div class="user-info">
            <% 
                User loginUser = (User) session.getAttribute("loginUser");
                if (loginUser != null) {
            %>
                欢迎您，<%= loginUser.getUsername() %>！
                <a href="LogoutServlet">退出登录</a>
            <% } %>
        </div>

        <h1>搜索和修改数据</h1>
        
        <!-- 提示信息 -->
        <% 
            String msg = (String) request.getAttribute("message");
            if (msg != null) {
                String msgClass = msg.contains("成功") ? "success" : "error";
        %>
            <div class="msg <%= msgClass %>"><%= msg %></div>
        <% } %>

        <!-- 搜索表单 -->
        <div class="search-form">
            <form action="SearchModifyServlet" method="get" id="searchForm">
                <%
                    // XSS 防护：对 keyword 做 HTML 转义
                    String rawKeyword = request.getParameter("keyword");
                    String escapedKeyword = "";
                    if (rawKeyword != null) {
                        escapedKeyword = rawKeyword
                            .replace("&", "&amp;")
                            .replace("<", "&lt;")
                            .replace(">", "&gt;")
                            .replace("\"", "&quot;")
                            .replace("'", "&#x27;");
                    }
                %>
                <input type="text" name="keyword" placeholder="请输入SKU、Seller或ISC1关键词" 
                       value="<%= escapedKeyword %>">
                <button type="submit">搜索</button>
                <a href="SearchModifyServlet" class="btn">显示全部</a>

                <!-- 下拉筛选框 -->
                <select name="filter" id="filterSelect" onchange="document.getElementById('searchForm').submit()">
                    <option value="">全部数据</option>
                    <option value="emptySeller" <%= "emptySeller".equals(request.getParameter("filter")) ? "selected" : "" %>>Seller 为空</option>
                </select>
            </form>
        </div>

        <table>
            <tr>
                <th>ID</th>
                <th>SKU</th>
                <th>Seller</th>
                <th>ISC1</th>
                <th>销售部门</th>
                <th>用户组织</th>
                <th>操作</th>
            </tr>
            <% 
                String keyword = request.getParameter("keyword");
                String keywordEncoded = "";
                if (keyword != null && !keyword.trim().isEmpty()) {
                    try {
                        keywordEncoded = java.net.URLEncoder.encode(keyword, "UTF-8");
                    } catch (Exception e) {
                        keywordEncoded = "";
                    }
                }

                Integer currentPage = (Integer) request.getAttribute("currentPage");
                Integer pageSize = (Integer) request.getAttribute("pageSize");
                String filter = request.getParameter("filter");

                List<CollectionData> dataList = (List<CollectionData>) request.getAttribute("dataList");
                Integer totalCount = (Integer) request.getAttribute("totalCount");
                Integer totalPages = (Integer) request.getAttribute("totalPages");
                
                // 处理空值，避免NullPointerException
                if (currentPage == null) currentPage = 1;
                if (pageSize == null) pageSize = 20;
                if (totalPages == null) totalPages = 1;
                
                if (dataList != null && !dataList.isEmpty()) {
                    for (CollectionData data : dataList) {
            %>
                <tr>
                    <td><%= data.getId() %></td>
                    <td class="break-word"><%= data.getSku() %></td>
                    <td><%= data.getSeller() != null ? data.getSeller() : "" %></td>
                    <td class="break-word"><%= data.getIsc1() %></td>
                    <td>
                        <%
                            // 处理销售部门显示逻辑
                            String salesDepart = data.getSalesDepart();
                            String departName = "";
                            if (salesDepart != null && !salesDepart.isEmpty()) {
                                try {
                                    int departCode = Integer.parseInt(salesDepart);
                                    // 当 sales_depart 为 0 时不显示
                                    if (departCode == 0) {
                                        departName = "";
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
                                    // 如果转换失败，检查是否为0
                                    if ("0".equals(salesDepart)) {
                                        departName = "";
                                    } else {
                                        departName = "未知部门(" + salesDepart + ")";
                                    }
                                }
                            }
                            out.print(departName);
                        %>
                    </td>
                    <td><%= data.getUserOrganization() != null ? data.getUserOrganization() : "" %></td>
                    <td>
                        <a href="SearchModifyServlet?action=edit&id=<%= data.getId() %>&page=<%= currentPage %>&pageSize=<%= pageSize %>&keyword=<%= keywordEncoded %>&filter=<%= filter %>" class="btn btn-edit">修改</a>
                        <a href="SearchModifyServlet?action=viewLog&id=<%= data.getId() %>&page=<%= currentPage %>&pageSize=<%= pageSize %>&keyword=<%= keywordEncoded %>&filter=<%= filter %>" class="btn">查看日志</a>
                    </td>
                </tr>
            <% 
                    }
                } else {
            %>
                <tr>
                    <td colspan="7" class="no-data">暂无数据</td>
                </tr>
            <% } %>
        </table>

        <!-- 分页控件 -->
        <%
            int[] pageSizes = {20, 50, 100, 200};
        %>

        <div class="pagination">
            <!-- 每页显示数量选择 -->
            <span style="margin-right: 15px;">
                每页显示：
                <select name="pageSize" id="pageSizeSelect" onchange="changePageSize(this.value)">
                    <% for (int size : pageSizes) { %>
                        <option value="<%= size %>" <%= (pageSize == size) ? "selected" : "" %>>
                            <%= size %> 条
                        </option>
                    <% } %>
                </select>
            </span>

            <!-- 首页 -->
            <a href="SearchModifyServlet?page=1&pageSize=<%= pageSize %>&keyword=<%= keywordEncoded %>&filter=<%= filter %>">首页</a>

            <!-- 上一页 -->
            <% if (currentPage > 1) { %>
                <a href="SearchModifyServlet?page=<%= currentPage - 1 %>&pageSize=<%= pageSize %>&keyword=<%= keywordEncoded %>&filter=<%= filter %>">上一页</a>
            <% } %>

            <!-- 页码列表 -->
            <%
                int startPage = Math.max(1, currentPage - 2);
                int endPage = Math.min(totalPages, currentPage + 2);

                for (int i = startPage; i <= endPage; i++) {
                    if (i == currentPage) {
            %>
                <a href="SearchModifyServlet?page=<%= i %>&pageSize=<%= pageSize %>&keyword=<%= keywordEncoded %>&filter=<%= filter %>" class="active"><%= i %></a>
            <%
                    } else {
            %>
                <a href="SearchModifyServlet?page=<%= i %>&pageSize=<%= pageSize %>&keyword=<%= keywordEncoded %>&filter=<%= filter %>"><%= i %></a>
            <%
                    }
                }
            %>

            <!-- 下一页 -->
            <% if (currentPage < totalPages) { %>
                <a href="SearchModifyServlet?page=<%= currentPage + 1 %>&pageSize=<%= pageSize %>&keyword=<%= keywordEncoded %>&filter=<%= filter %>">下一页</a>
            <% } %>

            <!-- 末页 -->
            <a href="SearchModifyServlet?page=<%= totalPages %>&pageSize=<%= pageSize %>&keyword=<%= keywordEncoded %>&filter=<%= filter %>">末页</a>

            <!-- 显示信息 -->
            <span style="margin-left: 10px;">
                共 <%= totalCount %> 条记录，<%= totalPages %> 页，当前第 <%= currentPage %> 页
            </span>

            <!-- 新增：页码跳转控件 -->
            <div class="page-input-group">
                跳至：
                <input type="number" 
                       id="targetPageInput" 
                       class="page-input" 
                       value="<%= currentPage %>"
                       min="1"
                       max="<%= totalPages %>"
                       maxlength="5">
                <button type="button" id="goToPageBtn" class="page-btn">跳转</button>
            </div>
        </div>

        <script>
        // 页面参数初始化（从后端获取）
        const currentPage = <%= currentPage %>;
        const totalPages = <%= totalPages %>;
        const pageSize = <%= pageSize %>;
        const keywordEncoded = '<%= keywordEncoded %>';
        const filter = '<%= filter != null ? filter : "" %>';

        // 每页条数变更
        function changePageSize(size) {
            const url = new URL(window.location.href);
            url.searchParams.set('pageSize', size);
            url.searchParams.set('page', 1); // 切换每页条数后回到第一页
            window.location.href = url.toString();
        }

        // 页码跳转功能
        document.addEventListener('DOMContentLoaded', function() {
            const goToPageBtn = document.getElementById('goToPageBtn');
            const targetPageInput = document.getElementById('targetPageInput');
            
            // 点击跳转按钮
            goToPageBtn.addEventListener('click', function() {
                validateAndJump();
            });
            
            // 输入框按Enter键触发跳转
            targetPageInput.addEventListener('keydown', function(e) {
                if (e.key === 'Enter') {
                    validateAndJump();
                }
            });

            // 验证并执行跳转
            function validateAndJump() {
                const pageStr = targetPageInput.value.trim();
                
                // 验证1：不能为空
                if (!pageStr) {
                    showNotification('错误', '页码不能为空，请输入要跳转的页码');
                    targetPageInput.focus();
                    return;
                }
                
                // 验证2：必须是数字
                const targetPage = parseInt(pageStr, 10);
                if (isNaN(targetPage)) {
                    showNotification('错误', '页码格式错误，请输入有效的数字');
                    targetPageInput.select();
                    return;
                }
                
                // 验证3：页码范围必须在1~总页数之间
                if (targetPage < 1 || targetPage > totalPages) {
                    showNotification('错误', `页码超出范围，请输入1~${totalPages}之间的页码`);
                    targetPageInput.select();
                    return;
                }
                
                // 验证通过，执行跳转
                const url = new URL(window.location.href);
                url.searchParams.set('page', targetPage);
                url.searchParams.set('pageSize', pageSize);
                url.searchParams.set('keyword', keywordEncoded);
                url.searchParams.set('filter', filter);
                window.location.href = url.toString();
            }

            // 显示提示框
            function showNotification(title, message) {
                // 创建提示框元素（如果不存在）
                let notification = document.getElementById('notification');
                if (!notification) {
                    notification = document.createElement('div');
                    notification.id = 'notification';
                    notification.className = 'notification error';
                    notification.innerHTML = `
                        <div class="title">${title}</div>
                        <div class="message">${message}</div>
                    `;
                    document.body.appendChild(notification);
                }
                
                // 设置提示内容
                notification.querySelector('.title').textContent = title;
                notification.querySelector('.message').textContent = message;
                
                // 显示提示框
                notification.classList.add('show');
                
                // 3秒后自动隐藏
                setTimeout(() => {
                    notification.classList.remove('show');
                }, 3000);
            }
        });
        </script>

        <div class="link">
            <a href="index.jsp" class="btn">返回表单</a>
            <a href="ListDataServlet" class="btn">查看数据列表</a>
        </div>
    </div>
</body>
</html>
