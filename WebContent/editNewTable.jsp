  <!-- 福来广告编辑界面 -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.model.CollectionData" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>编辑广告数据</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f5f5f5; padding: 20px; }
        .container { max-width: 600px; margin: 0 auto; background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h2 { color: #333; margin-bottom: 25px; border-bottom: 1px solid #eee; padding-bottom: 10px; }
        .form-group { margin-bottom: 20px; }
        .form-group label { display: block; margin-bottom: 5px; font-weight: bold; color: #555; }
        .form-group input[type="text"] { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 16px; }
        .form-group input[readonly] { background: #f9f9f9; color: #666; }
        .btn-group { text-align: center; margin-top: 30px; }
        .btn { padding: 10px 20px; border: none; border-radius: 4px; font-size: 16px; cursor: pointer; margin: 0 10px; }
        .btn-save { background: #67c23a; color: white; }
        .btn-cancel { background: #f56c6c; color: white; }
        .btn:hover { opacity: 0.9; }
        .back-link { display: inline-block; margin-bottom: 20px; color: #67c23a; text-decoration: none; }
        .back-link:hover { text-decoration: underline; }
        .msg { padding: 10px; margin-bottom: 20px; border-radius: 4px; }
        .success { background: #f0f9eb; color: #67c23a; }
        .error { background: #fef0f0; color: #f56c6c; }
    </style>
</head>
<body>
    <div class="container">
        <% 
            // 1️⃣ 获取所有筛选和分页参数
            String currentPage = request.getParameter("page");
            String pageSize = request.getParameter("pageSize");
            String searchKeyword = request.getParameter("searchKeyword");
            String needEdit = request.getParameter("needEdit");
            
            // 设置默认值
            if (currentPage == null || currentPage.isEmpty()) currentPage = "1";
            if (pageSize == null || pageSize.isEmpty()) pageSize = "20";
            
            // 2️⃣ ✅ 关键：构建返回URL（在Java代码中完成，避免HTML中的复杂表达式）
            StringBuilder returnUrl = new StringBuilder("ListNewTableServlet?page=");
            returnUrl.append(currentPage).append("&pageSize=").append(pageSize);
            
            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                returnUrl.append("&searchKeyword=").append(java.net.URLEncoder.encode(searchKeyword, "UTF-8"));
            }
            
            if (needEdit != null && !needEdit.isEmpty()) {
                returnUrl.append("&needEdit=").append(needEdit);
            }
        %>
        
        <!-- 3️⃣ 返回链接直接使用构建好的URL -->
        <a href="<%= returnUrl.toString() %>" class="back-link">⬅ 返回</a>
        <h2> 编辑广告数据</h2>

        <!-- 提示信息 -->
        <% 
            String msg = request.getParameter("message");
            if (msg != null) {
                String msgClass = msg.contains("成功") ? "success" : "error";
        %>
            <div class="msg <%= msgClass %>"><%= java.net.URLDecoder.decode(msg, "UTF-8") %></div>
        <% } %>

        <%
            CollectionData data = (CollectionData) request.getAttribute("data");
            if (data == null) {
                // 数据不存在时的处理
                String redirectUrl = "ListNewTableServlet?page=" + currentPage + "&pageSize=" + pageSize;
                if (searchKeyword != null && !searchKeyword.isEmpty()) {
                    redirectUrl += "&searchKeyword=" + java.net.URLEncoder.encode(searchKeyword, "UTF-8");
                }
                if (needEdit != null && !needEdit.isEmpty()) {
                    redirectUrl += "&needEdit=" + needEdit;
                }
                redirectUrl += "&message=" + java.net.URLEncoder.encode("数据不存在", "UTF-8");
                response.sendRedirect(redirectUrl);
                return;
            }
        %>

        <!-- 4️⃣ 表单中添加所有筛选和分页参数作为隐藏字段 -->
        <form action="UpdateNewTableServlet" method="post">
            <input type="hidden" name="id" value="<%= data.getId() %>">
            <input type="hidden" name="page" value="<%= currentPage %>">
            <input type="hidden" name="pageSize" value="<%= pageSize %>">
            <% if (searchKeyword != null && !searchKeyword.isEmpty()) { %>
                <input type="hidden" name="searchKeyword" value="<%= searchKeyword %>">
            <% } %>
            <% if (needEdit != null && !needEdit.isEmpty()) { %>
                <input type="hidden" name="needEdit" value="<%= needEdit %>">
            <% } %>

            <div class="form-group">
                <label>账号</label>
                <input type="text" value="<%= data.getAccount() != null ? data.getAccount() : "" %>" readonly>
            </div>

            <div class="form-group">
                <label>广告标题</label>
                <input type="text" value="<%= data.getCampaignName() != null ? data.getCampaignName() : "" %>" readonly>
            </div>

            <div class="form-group">
                <label>仓库SKU</label>
                <input type="text" name="warehouseSku" value="<%= data.getWarehouseSku() != null ? data.getWarehouseSku() : "" %>" required>
            </div>

            <div class="btn-group">
                <button type="submit" class="btn btn-save">保存修改</button>
                <!-- 5️⃣ ✅ 关键：取消按钮使用构建好的URL变量 -->
                <button type="button" class="btn btn-cancel" 
                        onclick="location.href='<%= returnUrl.toString() %>'"> 取消</button>
            </div>
        </form>
    </div>
</body>
</html>