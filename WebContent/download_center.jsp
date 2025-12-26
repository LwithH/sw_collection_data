<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.servlet.DownloadCenterUserServlet.DownloadResource" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>下载中心</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 900px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 25px; color: #333; }
        .header h1 { margin-bottom: 10px; color: #409eff; }
        .filters { margin-bottom: 20px; padding: 15px; background: #f8f9fa; border-radius: 6px; }
        .filters label { margin-right: 10px; font-weight: bold; color: #666; }
        .filters input, .filters button { padding: 6px 10px; margin-right: 10px; border: 1px solid #ddd; border-radius: 4px; }
        .btn { background: #409eff; color: white; border: none; padding: 8px 15px; border-radius: 4px; cursor: pointer; }
        .btn:hover { background: #3390e0; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
        th { background: #f8f9fa; color: #333; }
        .actions a { color: #409eff; text-decoration: none; }
        .empty { text-align: center; color: #999; padding: 40px 0; font-style: italic; }
        .file-info { font-size: 13px; color: #666; }
        
        /* 日期范围模态框样式 */
        .modal-overlay {
            display: none;
            position: fixed;
            top: 0; left: 0;
            width: 100%; height: 100%;
            background: rgba(0,0,0,0.5);
            z-index: 1000;
            justify-content: center;
            align-items: center;
        }
        .modal-content {
            background: white;
            padding: 25px;
            border-radius: 8px;
            width: 90%;
            max-width: 500px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.15);
        }
        .modal-header {
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 1px solid #eee;
        }
        .modal-header h3 {
            margin: 0;
            color: #409eff;
        }
        .date-range-modal .date-fields {
            display: flex;
            gap: 15px;
            margin-bottom: 20px;
        }
        .date-field {
            flex: 1;
        }
        .date-field label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
        }
        .date-field input[type="date"] {
            width: 100%;
            padding: 8px;
            border: 1px solid #ddd;
            border-radius: 4px;
        }
        .modal-actions {
            display: flex;
            justify-content: flex-end;
            gap: 10px;
            margin-top: 20px;
        }
        .btn-secondary {
            background: #909399;
        }
        .btn-secondary:hover {
            background: #7a7e83;
        }
        
        /* 动态数据标记 */
        .dynamic-tag {
            display: inline-block;
            background: #e6a23c;
            color: white;
            font-size: 11px;
            padding: 2px 6px;
            border-radius: 3px;
            margin-left: 5px;
            vertical-align: middle;
        }
        .date-range-tag {
            display: inline-block;
            background: #67c23a;
            color: white;
            font-size: 11px;
            padding: 2px 6px;
            border-radius: 3px;
            margin-left: 5px;
            vertical-align: middle;
        }
        
        /* 添加响应式表格样式，确保在小屏幕上也能良好显示 */
        @media (max-width: 768px) {
            table { font-size: 14px; }
            th, td { padding: 8px; }
            .filters { flex-direction: column; }
            .filters label, .filters input, .filters button { 
                margin-bottom: 8px; 
                width: 100%;
            }
            .date-range-modal .date-fields {
                flex-direction: column;
            }
        }
        
        /* 新增返回首页样式 - 仅修改这部分 */
        .back-home {
            margin-bottom: 15px;
            text-align: left; /* 左对齐 */
        }
    </style>
</head>
<body>
    <div class="container">
        <!-- 移动返回首页到这里，放在header上方 -->
        <div class="back-home">
            <a href="index.jsp" style="color: #409eff; text-decoration: none;">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16" style="vertical-align: middle; margin-right: 4px;">
                    <path fill-rule="evenodd" d="M15 8a.5.5 0 0 0-.5-.5H2.707l3.147-3.146a.5.5 0 1 0-.708-.708l-4 4a.5.5 0 0 0 0 .708l4 4a.5.5 0 0 0 .708-.708L2.707 8.5H14.5A.5.5 0 0 0 15 8z"/>
                </svg>
                返回首页
            </a>
        </div>

        <div class="header">
            <h1>下载中心</h1>
        </div>

        <!-- 筛选表单 -->
        <form method="get" class="filters">
            <label>名称：</label>
            <input type="text" name="name" value="${param.name}" placeholder="输入资源名称">
            
            <label>分类：</label>
            <input type="text" name="category" value="${param.category}" placeholder="输入分类名称">
            
            <button type="submit" class="btn">筛选</button>
            <a href="DownloadCenterUserServlet" style="margin-left: 10px; color: #67c23a;">重置</a>
        </form>

        <!-- 资源列表 -->
        <%
            List<DownloadResource> resources = (List<DownloadResource>) request.getAttribute("resources");
            if (resources == null || resources.isEmpty()) {
        %>
            <div class="empty">暂无可用的下载资源</div>
        <%
            } else {
        %>
            <table>
                <thead>
                    <tr>
                        <th>资源名称</th>
                        <th>分类</th>
                        <th>描述</th>
                        <th>大小</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (DownloadResource r : resources) {
                            // 格式化文件大小
                           String sizeStr;
                           long size = r.getFileSize();
                           
                           // 优化0值和负值处理
                           if (size < 0) {
                               sizeStr = "<span style='color:#e6a23c'>动态生成</span>";
                           } else if (size == 0) {
                               sizeStr = "<span class='size-error'>大小未记录</span>";
                           } else if (size >= 1024 * 1024 * 1024) { // GB
                               sizeStr = String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
                           } else if (size >= 1024 * 1024) { // MB
                               sizeStr = String.format("%.2f MB", size / (1024.0 * 1024));
                           } else if (size >= 1024) { // KB
                               sizeStr = String.format("%.1f KB", size / 1024.0);
                           } else { // Bytes
                               sizeStr = size + " B";
                           }
                           
                           // 检查是否是动态生成
                           boolean isDynamic = r.isDynamic();
                           // 检查是否支持日期范围
                           boolean supportsDateRange = r.isDateRangeEnabled();
                    %>
                    <tr>
                        <td>
                            <strong><%= r.getName() %></strong>

                            <% if (supportsDateRange) { %>
                                <span class="date-range-tag" title="支持按日期范围导出">日期</span>
                            <% } %>
                        </td>
                        <td><%= r.getCategory() != null ? r.getCategory() : "-" %></td>
                        <td><%= r.getDescription() != null ? r.getDescription() : "-" %></td>
                        <td><span class="file-info"><%= sizeStr %></span></td>
                        <td class="actions">
                            <% if (supportsDateRange) { %>
                                <!-- 支持日期范围的资源：点击后弹出日期选择框 -->
                                <a href="javascript:void(0);" 
                                   onclick="showDateRangeModal('<%= r.getId() %>', '<%= r.getName() %>')" 
                                   title="下载 <%= r.getName() %>（可选择日期范围）">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                        <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/>
                                        <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708l3 3z"/>
                                    </svg>
                                    下载（日期范围）
                                </a>
                            <% } else { %>
                                <!-- 不支持日期范围的资源：直接下载 -->
                                <a href="FileDownloadServlet?id=<%= r.getId() %>" title="下载 <%= r.getName() %>">
                                    <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16">
                                        <path d="M.5 9.9a.5.5 0 0 1 .5.5v2.5a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1v-2.5a.5.5 0 0 1 1 0v2.5a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2v-2.5a.5.5 0 0 1 .5-.5z"/>
                                        <path d="M7.646 11.854a.5.5 0 0 0 .708 0l3-3a.5.5 0 0 0-.708-.708L8.5 10.293V1.5a.5.5 0 0 0-1 0v8.793L5.354 8.146a.5.5 0 1 0-.708.708l3 3z"/>
                                    </svg>
                                    下载
                                </a>
                            <% } %>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        <%
            }
        %>

        <!-- 日期范围选择模态框 -->
        <div id="dateRangeModal" class="modal-overlay date-range-modal">
            <div class="modal-content">
                <div class="modal-header">
                    <h3>选择日期范围</h3>
                </div>
                <form id="dateRangeForm" method="get" action="FileDownloadServlet">
                    <input type="hidden" id="resourceId" name="id">
                    
                    <div class="date-fields">
                        <div class="date-field">
                            <label>开始日期 <span style="color:#f56c6c">*</span></label>
                            <input type="date" id="modalStartDate" name="startDate" required>
                        </div>
                        <div class="date-field">
                            <label>结束日期 <span style="color:#f56c6c">*</span></label>
                            <input type="date" id="modalEndDate" name="endDate" required>
                        </div>
                    </div>
                    
                    <div style="color: #666; font-size: 13px; margin-bottom: 15px; padding: 10px; background: #f8f9fa; border-radius: 4px;">
                        <strong>提示：</strong> 将导出指定日期范围内的数据。请确保选择的日期在数据有效范围内。
                    </div>
                    
                    <div class="modal-actions">
                        <button type="submit" class="btn">确认下载</button>
                        <button type="button" class="btn btn-secondary" onclick="closeDateRangeModal()">取消</button>
                    </div>
                </form>
            </div>
        </div>

    </div>

    <script>
        // 日期范围模态框函数
        let currentResourceId = null;
        let currentResourceName = null;
        
        function showDateRangeModal(resourceId, resourceName) {
            currentResourceId = resourceId;
            currentResourceName = resourceName;
            
            // 设置表单资源ID
            document.getElementById('resourceId').value = resourceId;
            
            // 清空之前的日期值
            document.getElementById('modalStartDate').value = '';
            document.getElementById('modalEndDate').value = '';
            
            // 设置默认日期（今天和30天前）
            const today = new Date().toISOString().split('T')[0];
            const thirtyDaysAgo = new Date();
            thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
            const thirtyDaysAgoStr = thirtyDaysAgo.toISOString().split('T')[0];
            
            document.getElementById('modalStartDate').value = thirtyDaysAgoStr;
            document.getElementById('modalEndDate').value = today;
            
            // 显示模态框
            document.getElementById('dateRangeModal').style.display = 'flex';
        }
        
        function closeDateRangeModal() {
            document.getElementById('dateRangeModal').style.display = 'none';
        }
        
        // 点击模态框外部关闭
        document.getElementById('dateRangeModal').addEventListener('click', function(e) {
            if (e.target === this) {
                closeDateRangeModal();
            }
        });
        
        // 日期验证
        document.getElementById('dateRangeForm').addEventListener('submit', function(e) {
            const startDate = document.getElementById('modalStartDate').value;
            const endDate = document.getElementById('modalEndDate').value;
            
            if (!startDate || !endDate) {
                e.preventDefault();
                alert('请选择开始日期和结束日期！');
                return false;
            }
            
            if (startDate > endDate) {
                e.preventDefault();
                alert('结束日期不能早于开始日期！');
                return false;
            }           
            return true;
        });
        
        // ESC键关闭模态框
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                closeDateRangeModal();
            }
        });
    </script>
</body>
</html>