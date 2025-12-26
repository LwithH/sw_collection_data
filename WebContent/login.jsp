<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>


<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>登录 - 数据收集系统V1.6</title>
    <style>
       
        body {
            font-family: "Helvetica Neue", Arial, sans-serif;
            margin: 0;
            padding: 0;
            min-height: 100vh;
            display: flex;
            justify-content: center;
            align-items: center;
           
            background: url("images/logo2.png") no-repeat center center fixed; 
            background-size: cover;
        }
  
    .box {
        background: white;
        padding: 35px 30px 5px; 
        border-radius: 10px;
        box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
        width: 450px;
        min-height: 330px; 
        position: relative;
        transform: translateX(50px);
    }

   
    .box-top-img {
        position: absolute;
        top: 8px;
        left: 15px;
        width: 65px;
        height: auto;
        border-radius: 4px;
        background-color: white;
        z-index: 1;
    }

   
    h1 {
        text-align: center;
        color: #333;
        margin-bottom: 22px;
        padding-bottom: 10px;
        border-bottom: 2px solid #409eff;
        font-size: 21px;
        font-weight: 600;
    }

  
    .form-group {
        margin-bottom: 20px;
    }
    label {
        display: block;
        margin-bottom: 5px;
        font-weight: 500;
        color: #555;
        font-size: 13px;
    }
    input {
        width: 100%;
        padding: 10px 14px;
        border: 1px solid #e0e0e0;
        border-radius: 3px;
        box-sizing: border-box;
        transition: border-color 0.3s ease;
        font-size: 13px;
    }
    .btn {
        width: 100%;
        padding: 11px;
        background: #409eff;
        color: white;
        border: none;
        border-radius: 3px;
        cursor: pointer;
        font-size: 14px;
        font-weight: 500;
        transition: background 0.2s ease;
    }
    .msg {
        text-align: center;
        padding: 7px;
        margin-bottom: 10px; /* 从15px减小到10px（减少5px） */
        border-radius: 3px;
        font-size: 12px;
    }
    .link {
        text-align: center;
        margin-top: 10px; /* 从15px减小到10px（减少5px） */
        font-size: 12px;
    }
    input:focus {
        outline: none;
        border-color: #409eff;
    }

    input[type="password"] {
        border-color: #eee;
    }

    .btn:hover {
        background: #3390e0;
    }

    .success {
        background: #f0f9eb;
        color: #67c23a;
        border: 1px solid #e6f7d8;
    }

    .error {
        background: #fef0f0;
        color: #f56c6c;
        border: 1px solid #ffe6e6;
    }

    a {
        color: #409eff;
        text-decoration: none;
        transition: color 0.2s ease;
    }

    a:hover {
        color: #3390e0;
        text-decoration: underline;
    }
</style>

</head>
<body>
    <div class="box">
        <!-- 顶部图标（与登录框完全融合） -->
        <img src="images/logo1.png" class="box-top-img" alt="系统logo">
    <h1>数据收集系统V1.6 - 登录</h1>

    <!-- 处理URL传递的中文消息乱码 -->
    <% 
        String urlMsg = request.getParameter("msg");
        String reqMsg = (String) request.getAttribute("msg");
        
        if (urlMsg != null) {
            urlMsg = new String(urlMsg.getBytes("ISO-8859-1"), "UTF-8");
        }
    %>

    <!-- 显示提示信息 -->
    <% if (urlMsg != null) { %>
        <div class="msg success"><%= urlMsg %></div>
    <% } else if (reqMsg != null) { %>
        <div class="msg error"><%= reqMsg %></div>
    <% } %>

    <!-- 登录表单 -->
    <form action="LoginServlet" method="post">
        <input type="hidden" name="redirect" value="<%= request.getParameter("redirect") != null ? request.getParameter("redirect") : "index.jsp" %>">
        
        <div class="form-group">
            <label>登录名</label>
            <input type="text" name="username" required placeholder="请输入登录名">
        </div>
        <div class="form-group">
            <label>密码</label>
            <input type="password" name="password" required placeholder="请输入密码">
        </div>
        <button type="submit" class="btn">登录</button>
    </form>
</div>

</body>
</html> 