<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.model.CollectionData" %>
<%@ page import="com.model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>temu数据列表</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 1200px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .user-info { text-align: right; margin-bottom: 20px; color: #666; }
        .user-info a { color: #f56c6c; text-decoration: none; margin-left: 10px; }
        h1 { color: #333; margin-bottom: 25px; padding-bottom: 10px; border-bottom: 2px solid #409eff; }
        .btn { display: inline-block; padding: 8px 15px; background: #409eff; color: white; text-decoration: none; border-radius: 4px; margin-bottom: 20px; margin-right: 8px;  vertical-align: middle; /* 新增：让所有按钮垂直居中对齐 */}
        .btn:hover { background: #3390e0; }
        /* 新增导出按钮样式 */
        .btn-export { 
            background: #67c23a; 
            position: relative;
            overflow: hidden;
             margin-top: 2px; /* 根据实际视觉效果调整（如2px、3px），仅微调自身垂直位置 */
        }
        .btn-export:hover {
            background: #5daf34;
        }
        .btn-export:active {
            transform: translateY(1px);
        }
        .loading-spinner {
            display: none;
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 16px;
            height: 16px;
            border: 2px solid white;
            border-top-color: transparent;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        }
        @keyframes spin {
            to { transform: translate(-50%, -50%) rotate(360deg); }
        }
        table { width: 100%; border-collapse: collapse; margin-top: 10px; }
        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background: #f8f9fa; color: #666; font-weight: bold; }
        tr:hover { background: #f5f5f5; }
        .no-data { text-align: center; padding: 40px; color: #999; }
        .pagination { margin-top: 20px; text-align: center; }
        .pagination .info {
            display: inline-block;
            margin-right: 20px;
            color: #666;
            font-size: 14px;
        }
        .pagination select, .pagination a {
            display: inline-block;
            padding: 5px 12px;
            margin: 0 3px;
            border: 1px solid #ddd;
            border-radius: 4px;
            text-decoration: none;
            color: #409eff;
            font-size: 14px;
        }
        .pagination a.active {
            background: #409eff;
            color: white;
            border-color: #409eff;
        }
        .pagination a:hover:not(.active) {
            background-color: #f1f1f1;
        }
        .pagination select:hover {
            background-color: #f1f1f1;
        }
        .pagination span { margin-left: 10px; }
        
        /* 新增：进度条样式 */
        .export-progress-container {
            position: fixed;
            top: 20px;
            right: 20px;
            width: 300px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
            z-index: 10000;
            display: none;
            overflow: hidden;
        }
        .export-progress-header {
            padding: 12px 15px;
            background: #f5f7fa;
            border-bottom: 1px solid #ebeef5;
            border-radius: 8px 8px 0 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .export-progress-title {
            font-weight: bold;
            color: #303133;
        }
        .export-progress-close {
            cursor: pointer;
            color: #909399;
            font-size: 14px;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            transition: all 0.3s;
        }
        .export-progress-close:hover {
            background: #f0f2f5;
            color: #606266;
        }
        .export-progress-body {
            padding: 15px;
        }
        .export-progress-bar {
            height: 8px;
            background: #ebeef5;
            border-radius: 4px;
            overflow: hidden;
            margin-top: 8px;
        }
        .export-progress-value {
            height: 100%;
            background: #67c23a;
            border-radius: 4px;
            width: 0%;
            transition: width 0.3s ease;
        }
        .export-progress-text {
            display: flex;
            justify-content: space-between;
            margin-top: 5px;
            font-size: 12px;
            color: #606266;
        }
        
        /* 新增：精美提示框样式 */
        .notification {
            position: fixed;
            top: 20px;
            right: 20px;
            width: 300px;
            padding: 15px;
            border-radius: 8px;
            box-shadow: 0 2px 12px 0 rgba(0,0,0,0.1);
            z-index: 10000;
            transform: translateX(400px);
            opacity: 0;
            transition: all 0.3s ease;
            display: flex;
            align-items: center;
        }
        .notification.show {
            transform: translateX(0);
            opacity: 1;
        }
        .notification-icon {
            width: 24px;
            height: 24px;
            margin-right: 10px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
        }
        .notification-icon.success {
            background: #f0f9eb;
            color: #67c23a;
        }
        .notification-icon.error {
            background: #fef0f0;
            color: #f56c6c;
        }
        .notification-content {
            flex: 1;
        }
        .notification-title {
            font-weight: bold;
            margin-bottom: 5px;
        }
        .notification-message {
            font-size: 13px;
        }
        .notification-close {
            cursor: pointer;
            color: #909399;
            font-size: 14px;
            width: 20px;
            height: 20px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            transition: all 0.3s;
        }
        .notification-close:hover {
            background: #f0f2f5;
            color: #606266;
        }
        
        /* 新增：遮罩层样式 */
        .export-overlay {
            position: fixed;
            top: 0;
            left: 0;
            right: 0;
            bottom: 0;
            background: rgba(0,0,0,0.1);
            z-index: 9999;
            display: none;
            backdrop-filter: blur(2px);
        }
        
        /* 新增：导出面板样式 */
        .export-panel {
            position: fixed;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            width: 400px;
            background: white;
            border-radius: 8px;
            box-shadow: 0 5px 30px -10px rgba(0,0,0,0.3);
            z-index: 10000;
            overflow: hidden;
            display: none;
        }
        .export-panel-header {
            padding: 15px 20px;
            background: #f5f7fa;
            border-bottom: 1px solid #ebeef5;
        }
        .export-panel-title {
            font-size: 18px;
            font-weight: bold;
            color: #303133;
        }
        .export-panel-body {
            padding: 20px;
        }
        .export-panel-footer {
            padding: 10px 20px;
            background: #f5f7fa;
            border-top: 1px solid #ebeef5;
            display: flex;
            justify-content: flex-end;
        }
        .export-panel-btn {
            padding: 6px 12px;
            border-radius: 4px;
            cursor: pointer;
            border: none;
            font-size: 14px;
        }
        .export-panel-btn.cancel {
            background: #f5f7fa;
            color: #606266;
            margin-right: 10px;
        }
        .exportppanel-btn.export {
            background: #67c23a;
            color: white;
        }
        .export-panel-btn.export:hover {
            background: #5daf34;
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- 登录用户信息 + 退出链接 -->
        <div class="user-info">
            <% 
                User loginUser = (User) session.getAttribute("loginUser");
                if (loginUser != null) {
            %>
                欢迎您，<%= loginUser.getUsername() %>！
                <a href="LogoutServlet">退出登录</a>
            <% } %>
        </div>

        <h1>已收集的数据列表</h1>
        <div>
            <a href="index.jsp" class="btn">返回表单</a>
            <a href="SearchModifyServlet" class="btn">搜索和修改数据</a>
            <!-- 新增导出按钮 -->
            <a href="javascript:void(0)" 
               class="btn btn-export" 
               id="exportBtn"
               data-keyword="<%= request.getParameter("keyword") != null ? java.net.URLEncoder.encode(request.getParameter("keyword"), "UTF-8") : "" %>">
               <span id="exportText">导出Excel</span>
               <div class="loading-spinner" id="exportSpinner"></div>
            </a>
        </div>

        <table>
            <tr>
                <th>ID</th>
                <th>SKU</th>
                <th>Seller</th>
                <th>ISC1</th>
                <th>销售部门</th>
                <th>用户组织</th>
            </tr>
            <% 
                List<CollectionData> dataList = (List<CollectionData>) request.getAttribute("dataList");
                Integer totalCount = (Integer) request.getAttribute("totalCount");
                
                if (dataList != null && !dataList.isEmpty()) {
                    for (CollectionData data : dataList) {
            %>
                <tr>
                    <td><%= data.getId() %></td>
                    <td><%= data.getSku() %></td>
                    <td><%= data.getSeller() %></td>
                    <td><%= data.getIsc1() %></td>
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
                </tr>
            <% 
                    }
                } else {
            %>
                <tr>
                    <td colspan="6" class="no-data">暂无数据，请先添加</td>
                </tr>
            <% } %>
        </table>

        <!-- 分页控件 -->
        <%
            Integer currentPage = (Integer) request.getAttribute("currentPage");
            Integer totalPages = (Integer) request.getAttribute("totalPages");
            String keyword = request.getParameter("keyword");

            // 获取当前每页条数
            Integer pageSize = 20;
            try {
                String pageSizeStr = request.getParameter("pageSize");
                if (pageSizeStr != null && !pageSizeStr.trim().isEmpty()) {
                    pageSize = Integer.parseInt(pageSizeStr);
                    if (pageSize <= 0) pageSize = 20;
                    if (pageSize > 500) pageSize = 500;
                }
            } catch (NumberFormatException e) {
                pageSize = 20;
            }

            int[] pageSizes = {20, 50, 100, 200};
        %>

        <div class="pagination">
            <span class="info">
                每页显示：
                <select name="pageSize" id="pageSizeSelect" onchange="changePageSize(this.value)">
                    <% for (int size : pageSizes) { %>
                        <option value="<%= size %>" <%= (pageSize == size) ? "selected" : "" %>>
                            <%= size %> 条
                        </option>
                    <% } %>
                </select>
            </span>

            <a href="ListDataServlet?page=1&pageSize=<%= pageSize %>&keyword=<%= keyword != null ? java.net.URLEncoder.encode(keyword, "UTF-8") : "" %>">首页</a>
            <% if (currentPage > 1) { %>
                <a href="ListDataServlet?page=<%= currentPage - 1 %>&pageSize=<%= pageSize %>&keyword=<%= keyword != null ? java.net.URLEncoder.encode(keyword, "UTF-8") : "" %>">上一页</a>
            <% } %>

            <%
                int startPage = Math.max(1, currentPage - 2);
                int endPage = Math.min(totalPages, currentPage + 2);

                for (int i = startPage; i <= endPage; i++) {
                    if (i == currentPage) {
            %>
                <a href="ListDataServlet?page=<%= i %>&pageSize=<%= pageSize %>&keyword=<%= keyword != null ? java.net.URLEncoder.encode(keyword, "UTF-8") : "" %>" class="active"><%= i %></a>
            <%
                    } else {
            %>
                <a href="ListDataServlet?page=<%= i %>&pageSize=<%= pageSize %>&keyword=<%= keyword != null ? java.net.URLEncoder.encode(keyword, "UTF-8") : "" %>"><%= i %></a>
            <%
                    }
                }
            %>

            <% if (currentPage < totalPages) { %>
                <a href="ListDataServlet?page=<%= currentPage + 1 %>&pageSize=<%= pageSize %>&keyword=<%= keyword != null ? java.net.URLEncoder.encode(keyword, "UTF-8") : "" %>">下一页</a>
            <% } %>
            <a href="ListDataServlet?page=<%= totalPages %>&pageSize=<%= pageSize %>&keyword=<%= keyword != null ? java.net.URLEncoder.encode(keyword, "UTF-8") : "" %>">末页</a>
            <span style="margin-left: 10px;">
                共 <%= totalCount %> 条记录，<%= totalPages %> 页，当前第 <%= currentPage %> 页
            </span>
        </div>
    </div>
    
    <!-- 进度条组件 -->
    <div class="export-progress-container" id="exportProgress">
        <div class="export-progress-header">
            <div class="export-progress-title">数据导出中</div>
            <div class="export-progress-close" id="progressClose">&times;</div>
        </div>
        <div class="export-progress-body">
            <div class="export-progress-bar">
                <div class="export-progress-value" id="progressValue"></div>
            </div>
            <div class="export-progress-text">
                <span id="progressText">0%</span>
                <span id="progressStatus">准备导出...</span>
            </div>
        </div>
    </div>
    
    <!-- 遮罩层 -->
    <div class="export-overlay" id="exportOverlay"></div>
    
    <!-- 精美提示框 -->
    <div class="notification" id="notification">
        <div class="notification-icon" id="notificationIcon"></div>
        <div class="notification-content">
            <div class="notification-title" id="notificationTitle"></div>
            <div class="notification-message" id="notificationMessage"></div>
        </div>
        <div class="notification-close" id="notificationClose">&times;</div>
    </div>

    <script>
    function changePageSize(size) {
        const url = new URL(window.location.href);
        url.searchParams.set('pageSize', size);
        window.location.href = url.toString();
    }
    
    // 修复后的导出功能
    document.addEventListener('DOMContentLoaded', function() {
        const exportBtn = document.getElementById('exportBtn');
        const progressContainer = document.getElementById('exportProgress');
        const progressValue = document.getElementById('progressValue');
        const progressText = document.getElementById('progressText');
        const progressStatus = document.getElementById('progressStatus');
        const progressClose = document.getElementById('progressClose');
        const exportOverlay = document.getElementById('exportOverlay');
        const notification = document.getElementById('notification');
        const notificationTitle = document.getElementById('notificationTitle');
        const notificationMessage = document.getElementById('notificationMessage');
        const notificationIcon = document.getElementById('notificationIcon');
        const notificationClose = document.getElementById('notificationClose');
        
        if (exportBtn) {
            exportBtn.addEventListener('click', function() {
                const btn = this;
                const spinner = document.getElementById('exportSpinner');
                const text = document.getElementById('exportText');
                
                // 显示加载状态
                btn.disabled = true;
                spinner.style.display = 'block';
                text.textContent = '导出中...';
                
                // 获取当前查询参数
                const keyword = btn.getAttribute('data-keyword');
                const filter = new URLSearchParams(window.location.search).get('filter') || '';
                
                // 显示进度条
                progressContainer.style.display = 'block';
                progressValue.style.width = '0%';
                progressText.textContent = '0%';
                progressStatus.textContent = '准备导出...';
                
                // 显示遮罩层
                exportOverlay.style.display = 'block';
                
                // 创建隐藏的iframe用于导出
                const iframe = document.createElement('iframe');
                iframe.style.display = 'none';
                
                // 修复1：正确拼接URL，包含filter参数
                let exportUrl = 'ExportDataServlet?keyword=' + encodeURIComponent(keyword);
                if (filter) {
                    exportUrl += '&filter=' + encodeURIComponent(filter);
                }
                iframe.src = exportUrl;
                
                // 模拟进度（因为无法获取真实进度）
                let progress = 0;
                const progressInterval = setInterval(() => {
                    if (progress < 95) {
                        progress += Math.floor(Math.random() * 4) + 1; // 随机增加1-3%
                        if (progress > 95) progress = 95;
                        
                        progressValue.style.width = progress + '%';
                        progressText.textContent = progress + '%';
                        
                        if (progress < 30) {
                            progressStatus.textContent = '正在准备数据...';
                        } else if (progress < 60) {
                            progressStatus.textContent = '正在生成Excel文件...';
                        } else if (progress < 85) {
                            progressStatus.textContent = '正在处理表格样式...';
                        } else {
                            progressStatus.textContent = '正在完成导出...';
                        }
                    }
                }, 200);
                
                // 修复2：移除onload监听（Excel不是HTML，不会触发正常的onload）
                // 修复3：添加超时机制，假设3秒内无错误即为成功
                const successTimeout = setTimeout(() => {
                    clearInterval(progressInterval);
                    
                    // 完成进度
                    progressValue.style.width = '100%';
                    progressText.textContent = '100%';
                    progressStatus.textContent = '导出完成！';
                    
                    // 显示成功通知
                    showNotification('success', '导出成功', 'Excel文件已成功生成，请检查下载目录');
                    
                    // 重置按钮状态
                    setTimeout(() => {
                        btn.disabled = false;
                        spinner.style.display = 'none';
                        text.textContent = '导出Excel';
                        
                        // 隐藏进度条和遮罩
                        progressContainer.style.display = 'none';
                        exportOverlay.style.display = 'none';
                        
                        // 移除iframe
                        if (document.body.contains(iframe)) {
                            document.body.removeChild(iframe);
                        }
                    }, 1000);
                }, 3000); // 3秒超时 - 根据数据量调整
                
                // 修复4：仅在真正错误时处理
                iframe.onerror = function() {
                    clearInterval(progressInterval);
                    clearTimeout(successTimeout);
                    
                    // 更新进度条为错误状态
                    progressValue.style.width = '100%';
                    progressValue.style.background = '#f56c6c';
                    progressText.textContent = '错误';
                    progressStatus.textContent = '导出失败！';
                    
                    // 显示错误通知
                    showNotification('error', '导出失败', '请检查网络连接或联系系统管理员');
                    
                    // 重置按钮状态
                    setTimeout(() => {
                        btn.disabled = false;
                        spinner.style.display = 'none';
                        text.textContent = '导出Excel';
                        
                        // 隐藏进度条和遮罩
                        progressContainer.style.display = 'none';
                        exportOverlay.style.display = 'none';
                        
                        // 移除iframe
                        if (document.body.contains(iframe)) {
                            document.body.removeChild(iframe);
                        }
                    }, 2000);
                };
                
                // 修复5：添加到DOM
                document.body.appendChild(iframe);
                
                // 修复6：添加更长的总超时（防止永久挂起）
                setTimeout(() => {
                    if (document.body.contains(iframe)) {
                        clearInterval(progressInterval);
                        clearTimeout(successTimeout);
                        iframe.onerror();
                        console.error('导出请求超时');
                    }
                }, 30000); // 30秒总超时
            });
        }
        
        // 进度条关闭按钮
        if (progressClose) {
            progressClose.addEventListener('click', function() {
                progressContainer.style.display = 'none';
                exportOverlay.style.display = 'none';
            });
        }
        
        // 显示通知
        function showNotification(type, title, message) {
            notificationTitle.textContent = title;
            notificationMessage.textContent = message;
            
            if (type === 'success') {
                notification.className = 'notification show';
                notificationIcon.className = 'notification-icon success';
                notificationIcon.innerHTML = '✓';
            } else {
                notification.className = 'notification show error';
                notificationIcon.className = 'notification-icon error';
                notificationIcon.innerHTML = '!';
            }
            
            // 自动关闭
            setTimeout(() => {
                notification.className = 'notification';
            }, 5000);
        }
        
        // 通知关闭按钮
        if (notificationClose) {
            notificationClose.addEventListener('click', function() {
                notification.className = 'notification';
            });
        }
    });
    </script>
</body>
</html>