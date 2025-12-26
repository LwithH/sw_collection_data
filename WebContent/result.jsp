<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>提交结果</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 600px; margin: 80px auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); text-align: center; }
        .user-info { text-align: right; margin-bottom: 20px; color: #666; }
        .user-info a { color: #f56c6c; text-decoration: none; margin-left: 10px; }
        h1 { color: #333; margin-bottom: 25px; }
        .msg { font-size: 18px; margin-bottom: 30px; padding: 15px; border-radius: 4px; }
        .success { background: #f0f9eb; color: #67c23a; }
        .error { background: #fef0f0; color: #f56c6c; }
        .btn { display: inline-block; padding: 10px 20px; background: #409eff; color: white; text-decoration: none; border-radius: 4px; margin: 0 10px; }
        .btn:hover { background: #3390e0; }
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

        <h1>操作结果</h1>
        <% 
            String msg = (String) request.getAttribute("message");
            String msgClass = msg.contains("成功") ? "success" : "error";
        %>
        <div class="msg <%= msgClass %>"><%= msg %></div>

        <a href="index.jsp" class="btn">返回表单</a>
        <a href="ListDataServlet" class="btn">查看数据列表</a>
    </div>
</body>
</html>