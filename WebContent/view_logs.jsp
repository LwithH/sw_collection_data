<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.model.OperationLog" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>数据修改日志</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .log-item { border-bottom: 1px dashed #ddd; padding: 15px 0; }
        .old-value { color: #f56c6c; font-family: monospace; font-weight: bold; }
        .new-value { color: #67c23a; font-family: monospace; font-weight: bold; }
        .timestamp { font-size: 0.9em; color: #666; }
        .btn-back { margin-bottom: 20px; }
    </style>
</head>
<body class="bg-light">
    <div class="container mt-5">
        <h3 class="mb-4">数据 ID: <%= request.getAttribute("dataId") %></h3>

        <%
            // 获取分页和筛选参数（避免使用 page 作为变量名）
            String pageStr = request.getParameter("page");
            String pageSizeStr = request.getParameter("pageSize");
            String keyword = request.getParameter("keyword");
            String filter = request.getParameter("filter");

            // 设置默认值（使用 pageNum 替代 page）
            int pageNum = 1;
            int pageSizeNum = 10;

            try {
                if (pageStr != null && !pageStr.isEmpty()) {
                    pageNum = Integer.parseInt(pageStr);
                    if (pageNum < 1) pageNum = 1;
                }
                if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
                    pageSizeNum = Integer.parseInt(pageSizeStr);
                    if (pageSizeNum <= 0) pageSizeNum = 10;
                    if (pageSizeNum > 500) pageSizeNum = 500;
                }
            } catch (NumberFormatException e) {
                pageNum = 1;
                pageSizeNum = 10;
            }

            // 编码 keyword，防止 URL 中文乱码
            String encodedKeyword = "";
            if (keyword != null && !keyword.isEmpty()) {
                try {
                    encodedKeyword = java.net.URLEncoder.encode(keyword, "UTF-8");
                } catch (Exception e) {
                    encodedKeyword = "";
                }
            }
        %>

        <!-- 返回按钮：携带所有参数 -->
        <a href="SearchModifyServlet?page=<%= pageNum %>&pageSize=<%= pageSizeNum %>&keyword=<%= encodedKeyword %>&filter=<%= filter %>" 
           class="btn btn-secondary btn-back">
            ← 返回上一页
        </a>

        <%
            List<OperationLog> logs = (List<OperationLog>) request.getAttribute("logs");
            if (logs == null || logs.isEmpty()) {
        %>
            <div class="alert alert-info text-center">
                暂无此数据的修改记录。
            </div>
        <%
        } else {
            for (OperationLog log : logs) {
                String content = log.getOperationContent();
                String oldVal = "";
                String newVal = "";

                // 去除 "用户[xxx]" 前缀
                if (content.startsWith("用户[")) {
                    int endIdx = content.indexOf("]");
                    if (endIdx > 0) {
                        content = content.substring(endIdx + 1).trim();
                    }
                }

                // 解析“修改为”或“更新为”格式
                if (content.contains("修改为")) {
                    String[] parts = content.split("修改为", 2);
                    oldVal = parts[0].replaceAll("从|更新", "").trim();
                    newVal = parts[1].trim();
                } else if (content.contains("更新为")) {
                    String[] parts = content.split("更新为", 2);
                    oldVal = parts[0].replaceAll("从|更新", "").trim();
                    newVal = parts[1].trim();
                } else {
                    oldVal = content;
                    newVal = "";
                }
        %>
            <div class="log-item">
                <div class="d-flex justify-content-between align-items-center">
                    <strong><%= log.getOperationType() %></strong>
                    <%
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String formattedTime = log.getCreateTime().format(formatter);
                    %>
                    <small class="timestamp"><%= formattedTime %></small>
                </div>
                <div class="mt-2">
                    <span class="old-value">修改前：</span>
                    <span><%= oldVal %></span>
                </div>
                <div class="mt-1">
                    <span class="new-value">修改后：</span>
                    <span><%= newVal %></span>
                </div>
                <div class="mt-1 text-muted small">
                    来自 IP: <%= log.getIpAddress() %> | 用户: <%= log.getUsername() %>
                </div>
            </div>
        <%
            }
        }
        %>
    </div>
</body>
</html>
