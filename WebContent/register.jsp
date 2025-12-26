<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>注册 - 数据收集系统</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 400px; margin: 80px auto; padding: 20px; background: #f5f5f5; }
        .box { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { text-align: center; color: #333; margin-bottom: 25px; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 8px; font-weight: bold; color: #666; }
        input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
        .btn { width: 100%; padding: 12px; background: #67c23a; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
        .btn:hover { background: #5aa729; }
        .msg { text-align: center; padding: 10px; margin-bottom: 20px; background: #fef0f0; color: #f56c6c; border-radius: 4px; }
        .link { text-align: center; margin-top: 20px; }
        a { color: #409eff; text-decoration: none; }
    </style>
</head>
<body>
    <div class="box">
        <h1>数据收集系统 - 注册</h1>

        <!-- 错误提示 -->
        <% 
            String msg = (String) request.getAttribute("msg");
            if (msg != null) {
        %>
            <div class="msg"><%= msg %></div>
        <% } %>

        <!-- 注册表单：提交到RegisterServlet -->
        <form action="RegisterServlet" method="post">
            <div class="form-group">
                <label>登录名</label>
                <input type="text" name="username" required placeholder="请设置登录名">
            </div>
            <div class="form-group">
                <label>密码（至少6位）</label>
                <input type="password" name="password" minlength="6" required placeholder="请设置密码">
            </div>
            <div class="form-group">
                <label>确认密码</label>
                <input type="password" name="repassword" minlength="6" required placeholder="请再次输入密码">
            </div>
            <button type="submit" class="btn">注册</button>
        </form>

        <div class="link">
            已有账号？<a href="login.jsp">立即登录</a>
        </div>
    </div>
</body>
</html>