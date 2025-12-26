<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.servlet.DownloadCenterAdminServlet.Resource" %>
<%@ page import="com.servlet.DownloadCenterAdminServlet.Permission" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>管理资源 - 下载中心</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 1100px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 25px; }
        .header h1 { color: #409eff; margin: 0; }
        .actions { text-align: center; margin-bottom: 20px; }
        .btn { padding: 10px 16px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; margin: 0 5px; }
        .btn:hover { background: #3390e0; }
        .back-home { color: #67c23a; margin-left: 10px; }
        table { width: 100%; border-collapse: collapse; margin-top: 15px; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #eee; }
        th { background: #f8f9fa; color: #333; }
        .status-public { color: #67c23a; font-weight: bold; }
        .status-hidden { color: #f56c6c; }
        .type-local { background: #eef5ff; color: #409eff; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
        .type-static { background: #f0f9eb; color: #67c23a; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
        .type-dynamic { background: #f4f4f5; color: #909399; padding: 2px 6px; border-radius: 4px; font-size: 12px; }
        .file-info { font-size: 13px; color: #666; }
        .empty { text-align: center; color: #999; padding: 40px 0; font-style: italic; }
        .confirm-delete { color: #f56c6c; text-decoration: none; }
        .confirm-delete:hover { text-decoration: underline; }
        
        /* 权限标签样式 */
        .permissions-info { margin-top: 5px; }
        .permission-tag { display: inline-block; background: #eef5ff; color: #409eff; padding: 2px 6px; border-radius: 3px; margin: 2px; font-size: 11px; max-width: 100px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
        .permission-dept { background: #e1f3d8; color: #67c23a; }
        .permission-user { background: #f0f4ff; color: #5a7ffb; }
        .no-permissions { color: #999; font-style: italic; font-size: 12px; }
        
        /* 操作列样式 */
        .action-links { display: flex; flex-direction: column; gap: 5px; }
        .action-links a { display: inline-block; text-decoration: none; }
        .action-visibility { color: #67c23a; }
        .action-hide { color: #f56c6c; }
        .action-permissions { color: #409eff; }
        .action-delete { color: #f56c6c; }
        .action-download { color: #5a7ffb; }
        
        /* 资源类型标签 */
        .resource-type { display: inline-block; margin-right: 5px; }
        
        /* 响应式 */
        @media (max-width: 900px) {
            table { font-size: 14px; }
            th, td { padding: 8px; }
            .permission-tag { max-width: 70px; font-size: 10px; }
        }
    </style>
    <script>
        function confirmDelete(name) {
            return confirm('确定要删除资源「' + name + '」吗？此操作不可恢复！');
        }
    </script>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>资源管理</h1>
            <p>管理所有上传的下载资源</p>
        </div>

        <div class="actions">
            <a href="DownloadCenterAdminServlet?action=uploadForm" class="btn">上传新资源</a>
            <a href="index.jsp" class="btn back-home"> 返回首页</a>
        </div>

        <%
            List<Resource> resources = (List<Resource>) request.getAttribute("resources");
            if (resources == null || resources.isEmpty()) {
        %>
            <div class="empty">暂无任何资源</div>
        <%
            } else {
        %>
            <table>
                <thead>
                    <tr>
                        <th>资源名称</th>
                        <th>分类</th>
                        <th>类型/格式</th>
                        <th>大小</th>
                        <th>权限设置</th>
                        <th>上传信息</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        for (Resource r : resources) {
                            // 格式化文件大小
                            String sizeStr = "-";
                            if (r.getFileSize() > 0) {
                                long size = r.getFileSize();
                                if (size >= 1024 * 1024 * 1024) { // GB
                                    sizeStr = String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
                                } else if (size >= 1024 * 1024) { // MB
                                    sizeStr = String.format("%.2f MB", size / (1024.0 * 1024));
                                } else if (size >= 1024) { // KB
                                    sizeStr = String.format("%.1f KB", size / 1024.0);
                                } else { // Bytes
                                    sizeStr = size + " B";
                                }
                            } else if (r.getFileSize() < 0) {
                                sizeStr = "动态生成";
                            }
                            
                            // 状态显示
                            String statusClass = "status-hidden";
                            String statusText = "隐藏";
                            if ("yes".equalsIgnoreCase(r.getIsVisible())) {
                                statusClass = "status-public";
                                statusText = "公开";
                            }
                            
                            // 类型标签
                            String typeClass = "type-local";
                            String typeText = "本地上传";
                            if ("STATIC".equals(r.getResourceType())) {
                                typeClass = "type-static";
                                typeText = "静态导出";
                            } else if ("DYNAMIC".equals(r.getResourceType())) {
                                typeClass = "type-dynamic";
                                typeText = "动态生成";
                            }
                            
                            // 文件类型标签
                            String fileType = r.getFileType();
                            String fileTypeText = fileType != null ? fileType.toUpperCase() : "未知";
                    %>
                    <tr>
                        <td>
                            <strong><%= r.getName() %></strong><br>
                            <span class="file-info"><%= r.getDescription() != null ? r.getDescription() : "" %></span>
                        </td>
                        <td><%= r.getCategory() != null ? r.getCategory() : "-" %></td>
                        <td>
                            <span class="<%= typeClass %> resource-type"><%= typeText %></span>
                            <span class="file-info"><%= fileTypeText %></span>
                        </td>
                        <td><%= sizeStr %></td>
                        <td>
                            <div class="permissions-info">
                                <%
                                    List<Permission> permissions = r.getPermissions();
                                    if (permissions == null || permissions.isEmpty()) {
                                %>
                                    <span class="no-permissions">所有人可见</span>
                                <%
                                    } else {
                                        int displayCount = 0;
                                        for (Permission p : permissions) {
                                            if (displayCount < 3) { // 只显示前3个权限
                                                String tagClass = "permission-tag";
                                                if ("DEPARTMENT".equals(p.getTargetType())) {
                                                    tagClass += " permission-dept";
                                                } else if ("USER".equals(p.getTargetType())) {
                                                    tagClass += " permission-user";
                                                }
                                                String displayValue = p.getTargetValue();
                                                if (displayValue.length() > 8) {
                                                    displayValue = displayValue.substring(0, 8) + "...";
                                                }
                                %>
                                    <span class="<%= tagClass %>" title="<%= p.getTargetType() %>: <%= p.getTargetValue() %>">
                                        <%= displayValue %>
                                    </span>
                                <%
                                            }
                                            displayCount++;
                                        }
                                        if (permissions.size() > 3) {
                                %>
                                    <span class="permission-tag" title="还有<%= permissions.size() - 3 %>个权限">+<%= permissions.size() - 3 %></span>
                                <%
                                        }
                                    }
                                %>
                            </div>
                        </td>
                        <td>
                            <div><strong><%= r.getUploaderName() %></strong></div>
                            <div class="file-info"><%= r.getUploadTime() != null ? r.getUploadTime().toString().substring(0, 19) : "-" %></div>
                        </td>
                        <td>
                            <span class="<%= statusClass %>"><%= statusText %></span>
                        </td>
                        <td>
                            <div class="action-links">
                                <!-- 切换可见性 -->
                                <%
                                    if ("yes".equalsIgnoreCase(r.getIsVisible())) {
                                %>
                                    <a href="DownloadCenterAdminServlet?action=updateVisibility&id=<%= r.getId() %>&isVisible=no"
                                       class="action-hide" title="隐藏资源">隐藏</a>
                                <%
                                    } else {
                                %>
                                    <a href="DownloadCenterAdminServlet?action=updateVisibility&id=<%= r.getId() %>&isVisible=yes"
                                       class="action-visibility" title="公开资源">公开</a>
                                <%
                                    }
                                %>
                                
                                <!-- 编辑权限 -->
                                <a href="DownloadCenterAdminServlet?action=editPermissions&id=<%= r.getId() %>"
                                   class="action-permissions" title="编辑权限">权限</a>
                                
                                <!-- 如果是本地上传或静态导出，提供下载链接（用于测试） -->
                                <%
                                    if ("LOCAL".equals(r.getResourceType()) || "STATIC".equals(r.getResourceType())) {
                                %>
                                    <a href="FileDownloadServlet?id=<%= r.getId() %>" 
                                       class="action-download" title="下载文件" target="_blank">下载</a>
                                <%
                                    } else if ("DYNAMIC".equals(r.getResourceType())) {
                                %>
                                    <a href="FileDownloadServlet?id=<%= r.getId() %>" 
                                       class="action-download" title="动态生成并下载" target="_blank">生成下载</a>
                                <%
                                    }
                                %>
                                
                                <!-- 删除 -->
                                <a href="DownloadCenterAdminServlet?action=delete&id=<%= r.getId() %>"
                                   class="action-delete confirm-delete"
                                   onclick="return confirmDelete('<%= r.getName().replace("'", "\\'") %>')"
                                   title="删除资源">删除</a>
                            </div>
                        </td>
                    </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
            
            <div style="margin-top: 20px; font-size: 13px; color: #666;">
                <p><strong>说明：</strong></p>
                <ul style="margin: 5px 0; padding-left: 20px;">
                    <li><span class="type-local" style="margin-right: 5px;">本地上传</span>：用户上传的文件</li>
                    <li><span class="type-static" style="margin-right: 5px;">静态导出</span>：从数据库导出生成的CSV文件</li>
                    <li><span class="type-dynamic" style="margin-right: 5px;">动态生成</span>：每次下载时实时从数据库查询生成</li>
                    <li>权限标签：<span class="permission-tag permission-dept">部门</span> <span class="permission-tag permission-user">用户</span></li>
                </ul>
            </div>
        <%
            }
        %>
    </div>
</body>
</html>