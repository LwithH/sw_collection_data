  <!--  -->
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.model.CollectionData" %>
<%@ page import="com.model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>编辑数据</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .user-info { text-align: right; margin-bottom: 20px; color: #666; }
        .user-info a { color: #f56c6c; text-decoration: none; margin-left: 10px; }
        h1 { color: #333; margin-bottom: 25px; padding-bottom: 10px; border-bottom: 2px solid #409eff; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 8px; font-weight: bold; color: #666; }
        input[type="text"] { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
        input[readonly] {
            background-color: #f5f5f5;
            border: 1px solid #ccc;
            color: #666;
            font-style: italic;
        }
        .required { color: #f56c6c; }
        .btn { padding: 12px 20px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; margin-right: 10px; }
        .btn-cancel { background: #909399; }
        .btn:hover { opacity: 0.9; }
        
        <!-- temu修改数据界面 -->
        .error-msg { 
            padding: 12px 15px; 
            margin-bottom: 20px; 
            background: #fff2f0; 
            color: #f56c6c; 
            border: 1px solid #ffccc7; 
            border-radius: 4px; 
            font-size: 14px;
            display: flex;
            align-items: center;
        }
        .error-msg::before { 
            content: "⚠️"; 
            margin-right: 8px; 
            font-size: 16px;
        }
        
        <!-- 核心修改2：区分“数据不存在”的错误样式（与普通错误一致，保持统一） -->
        .data-not-found { 
            padding: 12px 15px; 
            margin-bottom: 20px; 
            background: #fff2f0; 
            color: #f56c6c; 
            border: 1px solid #ffccc7; 
            border-radius: 4px; 
            font-size: 14px;
            display: flex;
            align-items: center;
        }
        .data-not-found::before { 
            content: "⚠️"; 
            margin-right: 8px; 
            font-size: 16px;
        }
        
        .link { margin-top: 20px; }
        a { color: #409eff; text-decoration: none; }
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

        <h1>编辑数据</h1>

        <!-- 核心修改3：优化“后端传递的错误提示”显示 -->
        <% 
            String msg = (String) request.getAttribute("message");
            if (msg != null && !msg.trim().isEmpty()) { // 避免空消息显示空框
        %>
            <div class="error-msg"><%= msg %></div> <!-- 使用新的错误样式 -->
        <% } %>

        <% 
            CollectionData data = (CollectionData) request.getAttribute("data");
            // 提前声明分页参数变量，避免分支内重复定义
            String currentPage = request.getParameter("page") != null ? request.getParameter("page") : "1";
            String currentPageSize = request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10";
            String currentKeyword = request.getParameter("keyword") != null ? request.getParameter("keyword") : "";
            String currentFilter = request.getParameter("filter") != null ? request.getParameter("filter") : "";

            if (data == null) {
        %>
            <!-- 核心修改4：优化“数据不存在”提示样式 -->
            <div class="data-not-found">数据不存在或已被删除！</div>
            <a href="SearchModifyServlet?page=<%= currentPage %>&pageSize=<%= currentPageSize %>&keyword=<%= currentKeyword %>&filter=<%= currentFilter %>" class="btn">返回搜索页面</a>
        <% 
            } else {
                // 处理ISC1空值回显（避免页面显示"null"，原有优化保留，不影响功能）
                String isc1Value = (data.getIsc1() == null || data.getIsc1().trim().isEmpty()) ? "" : data.getIsc1();
        %>
            <!-- 编辑表单：保留原有所有功能逻辑，不改动 -->
            <form action="SearchModifyServlet" method="post">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="<%= data.getId() %>">
                <input type="hidden" name="keyword" value="<%= currentKeyword %>">
                <input type="hidden" name="page" value="<%= currentPage %>">
                <input type="hidden" name="pageSize" value="<%= currentPageSize %>">
                <input type="hidden" name="filter" value="<%= currentFilter %>">

                <div class="form-group">
                    <label>ID</label>
                    <input type="text" value="<%= data.getId() %>" disabled>
                </div>
                <div class="form-group">
                    <label>SKU <span class="required">*</span></label>
                    <input type="text" name="sku" required value="<%= data.getSku() %>"readonly>
                </div>
                <div class="form-group">
                    <label>Seller <span class="required">*</span></label>
                    <input type="text" name="seller" required value="<%= data.getSeller() %>">
                </div>
                <div class="form-group">
                    <label>ISC1 <span class="required">*</span></label> <!-- 保留原有必填标记 -->
                    <input type="text" name="isc1" required value="<%= isc1Value %>" readonly> <!-- 保留原有readonly和required -->
                </div>
                <button type="submit" class="btn">保存修改</button>
                <a href="SearchModifyServlet?page=<%= currentPage %>&pageSize=<%= currentPageSize %>&keyword=<%= currentKeyword %>&filter=<%= currentFilter %>" class="btn btn-cancel">取消</a>
            </form>
        <% } %>
    </div>
</body>
</html>