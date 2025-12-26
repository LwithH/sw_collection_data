<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>系统提示 - 错误</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            max-width: 600px;
            margin: 80px auto;
            padding: 20px;
            background: #f5f5f5;
            text-align: center;
        }
        .container {
            background: white;
            padding: 40px 30px;
            border-radius: 8px;
            box-shadow: 0 2px 12px rgba(0,0,0,0.1);
        }
        .icon {
            font-size: 60px;
            color: #f56c6c;
            margin-bottom: 20px;
        }
        h1 {
            color: #f56c6c;
            margin: 0 0 15px;
            font-size: 24px;
        }
        .message {
            color: #666;
            line-height: 1.6;
            margin-bottom: 25px;
            padding: 15px;
            background: #fef0f0;
            border-radius: 6px;
            border-left: 4px solid #f56c6c;
        }
        .btn {
            display: inline-block;
            padding: 10px 24px;
            background: #409eff;
            color: white;
            text-decoration: none;
            border-radius: 4px;
            font-weight: bold;
        }
        .btn:hover {
            background: #3390e0;
        }
        .back-home {
            margin-top: 15px;
            display: block;
            color: #67c23a;
            text-decoration: none;
            font-size: 14px;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="icon">⚠️</div>
        <h1>操作失败</h1>
        
        <div class="message">
            <%
                String msg = (String) request.getAttribute("message");
                if (msg != null && !msg.trim().isEmpty()) {
                    out.println(msg);
                } else {
                    out.println("系统遇到一个意外错误，请稍后重试。");
                }
            %>
        </div>

        <a href="index.jsp" class="btn">🏠 返回首页</a>
        <a href="javascript:history.back()" class="back-home">← 返回上一页</a>
    </div>
</body>
</html>