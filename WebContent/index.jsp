<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.model.User" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>数据收集 - 首页</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .user-info { text-align: right; margin-bottom: 20px; color: #666; }
        .user-info a { color: #f56c6c; text-decoration: none; margin-left: 10px; }
        h1 { color: #333; margin-bottom: 25px; padding-bottom: 10px; border-bottom: 2px solid #409eff; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 8px; font-weight: bold; color: #666; }
        input { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box; }
        .required { color: #f56c6c; }
        .btn { padding: 12px 20px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
        .btn:hover { background: #3390e0; }
        .msg { padding: 10px; margin-bottom: 20px; border-radius: 4px; }
        .error { background: #fef0f0; color: #f56c6c; }
        .link-group { margin-top: 20px; }
        .link { display: inline-block; margin-right: 15px; color: #409eff; text-decoration: none; }
        .tip { font-size: 12px; color: #666; margin-top: 5px; font-style: italic; }
        
        /* 切换表区域样式 */
        .table-switch { 
            text-align: left; 
            margin: 15px 0; 
            padding: 10px; 
            background: #f8f9fa; 
            border-radius: 6px; 
            border-left: 4px solid #409eff;
        }
        .table-switch strong { color: #333; }
        .table-switch .link { margin-right: 12px; font-weight: 500; }
        
        /* 账号管理区域样式 */
        .account-section {
            text-align: left;
            margin: 15px 0;
            padding: 10px;
            background: #f0f7ff;
            border-radius: 6px;
            border-left: 4px solid #67c23a;
        }
        .account-section strong {
            color: #333;
        }
        .account-section .link {
            margin-right: 12px;
            font-weight: 500;
            color: #67c23a;
        }

        /* 下载中心区域样式 */
        .download-section {
            text-align: left;
            margin: 15px 0;
            padding: 10px;
            background: #f8f9fa;
            border-radius: 6px;
            border-left: 4px solid #909399;
        }
        .download-section strong {
            color: #333;
            display: block;
            margin-bottom: 8px;
        }
        .download-section .admin-actions {
            margin-top: 8px;
            padding: 10px;
            background: #f0f9ff;
            border-radius: 4px;
            border-left: 3px solid #409eff;
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

        <%-- 动态显示"切换表"按钮组，并处理单权限自动跳转 --%>
<%
    boolean canViewTemu = false;
    boolean canViewFulai = false;
    boolean canViewAmazon = false;
    boolean canViewAd = false;
    boolean canViewDsp = false; // 新增汇率表权限判断
    if (loginUser != null) {
        canViewTemu = "yes".equalsIgnoreCase(loginUser.getIsSeeTemu());
        canViewFulai = "yes".equalsIgnoreCase(loginUser.getIsSeeFulai());
        canViewAmazon = "yes".equalsIgnoreCase(loginUser.getIsSeeAmazon());
        canViewAd = "yes".equalsIgnoreCase(loginUser.getIsSeeAd());
        canViewDsp = "yes".equalsIgnoreCase(loginUser.getIsSeeDsp()); // 新增汇率表权限
        int tableCount = (canViewTemu ? 1 : 0) + 
                         (canViewFulai ? 1 : 0) + 
                         (canViewAmazon ? 1 : 0) + 
                         (canViewAd ? 1 : 0);

        if (tableCount == 1) {
            if (canViewTemu) {
                response.sendRedirect("ListDataServlet");
                return;
            } else if (canViewFulai) {
                response.sendRedirect("ListNewTableServlet");
                return;
            } else if (canViewAmazon) {
                response.sendRedirect("ListAmazonDataServlet?page=1&size=10");
                return;
            } else if (canViewAd) {
                response.sendRedirect("ListBrandAdServlet");
                return;
            }
        }
    }
%>

<%
    if (canViewTemu || canViewFulai || canViewAmazon || canViewAd) {
%>
<div class="table-switch">
    <strong>切换数据表：</strong>
    <% if (canViewTemu) { %>
        <a href="SearchModifyServlet" class="link">temu数据表</a>
    <% } %>
    <% if (canViewFulai) { %>
        <a href="ListNewTableServlet" class="link">福来广告数据表</a>
    <% } %>
    <% if (canViewAmazon) { %>
        <a href="ListAmazonDataServlet?page=1&size=10" class="link">亚马逊数据表</a>
    <% } %>
    <% if (canViewAd) { %>
        <a href="ListBrandAdServlet" class="link">品牌广告数据表</a>
    <% } %>
        <% if (canViewDsp) { %> <!-- 新增汇率表链接 -->
        <a href="ListDspDataServlet" class="link">汇率数据表</a>
    <% } %>
</div>
<%
    }
%>

        <%-- 账号管理系统区域 --%>
<%
    boolean canViewAccount = false;
    if (loginUser != null) {
        canViewAccount = "yes".equalsIgnoreCase(loginUser.getIsSeeAccount());
    }
    if (canViewAccount) {
%>
<div class="account-section">
    <strong>账号管理：</strong>
    <a href="AccountManagementServlet" class="link">账号管理系统</a>
</div>
<%
    }
%>


<%
    // 定义下载中心权限变量
    boolean canViewDownloadCenter = false; // 能否看到下载中心
    boolean canManageDownloadCenter = false; // 能否管理下载中心
    
    if (loginUser != null) {
       
        String downloadPermission = loginUser.getIsDownloadAdmin();
        canViewDownloadCenter = "yes".equalsIgnoreCase(downloadPermission); 
        String adminPermission = loginUser.getIsAdmin();
        canManageDownloadCenter = "yes".equalsIgnoreCase(adminPermission);
    }
%>

<% if (canViewDownloadCenter) { %>
<div class="download-section">
    <strong>下载中心</strong>
    <% if (canManageDownloadCenter) { %>
        <div class="admin-actions">
            <a href="DownloadCenterAdminServlet?action=uploadForm" class="link">上传新资源</a>
            <a href="DownloadCenterAdminServlet?action=manage" class="link">⚙ 管理资源</a>
        </div>
    <% } %>
    <div style="margin-top: 8px;">
        <a href="DownloadCenterUserServlet" class="link">浏览可下载资源</a>
    </div>
</div>
<% } %>
        <%
            if (canViewTemu) {
        %>
            <h1>temu数据提交表单</h1>
            <% 
                String msg = (String) request.getAttribute("message");
                if (msg != null) {
            %>
                <div class="msg error"><%= msg %></div>
            <% } %>
            <form action="SubmitDataServlet" method="post">
                <div class="form-group">
                    <label>SKU <span class="required">*</span></label>
                    <input type="text" name="sku" required 
                           value="<%= request.getAttribute("filledSku") != null ? request.getAttribute("filledSku") : "" %>"
                           placeholder="请输入SKU">
                </div>
                <div class="form-group">
                    <label>Seller <span class="required">*</span></label>
                    <input type="text" name="seller" required 
                           value="<%= request.getAttribute("filledSeller") != null ? request.getAttribute("filledSeller") : "" %>"
                           placeholder="请输入销售员">
                    <span class="tip">提示：销售员需为系统中已存在的名字</span>
                </div>
                
                <input type="hidden" name="isc1" value="首位">
                
                <button type="submit" class="btn">提交数据</button>
            </form>

            <div class="link-group">
                <a href="ListDataServlet" class="link">查看已收集的数据</a>
                <a href="SearchModifyServlet" class="link">搜索和修改数据</a>
            </div>
        <%
            } else {
        %>
            <div style="text-align: center; color: #999; padding: 40px 0; font-size: 16px;">
                <% if (loginUser != null) { %>
                     请切换到您有权限的数据表。
                <% } else { %>
                    请先登录系统
                <% } %>
            </div>
        <%
            }
        %>
        
    </div>
</body>
</html>