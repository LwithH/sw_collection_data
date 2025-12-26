<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.model.AccountData" %>
<%@ page import="com.model.V3" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page isELIgnored="true" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>账号管理系统</title>
    <style>
        body { 
            font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; 
            margin: 0; 
            padding: 20px; 
            background: #f5f7fa;
            color: #333;
        }
        .container {
            max-width: 1500px;
            margin: 0 auto;
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 15px rgba(0,0,0,0.1);
            overflow: hidden;
        }
        .header {
            background: linear-gradient(135deg, #1a6dcc 0%, #0d4a8a 100%);
            color: white;
            padding: 20px 30px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }
        .header h1 {
            margin: 0;
            font-size: 24px;
            font-weight: 600;
        }
        /* 修复紫鸟和店铺名称过长问题 */
.acc-name-cell, .ziniao-cell {
    max-width: 130px; /* 限制最大宽度，根据需要调整 */
    word-wrap: break-word; /* 允许单词内换行 */
    white-space: normal; /* 允许自动换行 */
    overflow: hidden; /* 隐藏溢出内容 */
    text-overflow: ellipsis; /* 超出部分显示省略号（可选） */
}
        
        .back-link {
            color: white;
            text-decoration: none;
            padding: 8px 15px;
            border-radius: 4px;
            background: rgba(255,255,255,0.1);
            transition: all 0.3s;
        }
        .back-link:hover {
            background: rgba(255,255,255,0.2);
            text-decoration: none;
        }
        .content {
            padding: 30px;
        }
        .stats {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: #f0f5ff;
            border-radius: 8px;
            padding: 20px;
            text-align: center;
            border-left: 4px solid #409eff;
        }
        .stat-value {
            font-size: 28px;
            font-weight: bold;
            color: #409eff;
            margin: 10px 0;
        }
        .stat-label {
            color: #666;
            font-size: 14px;
        }
        .table-container {
            overflow-x: auto;
            border-radius: 8px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.05);
        }
        table {
            width: 100%;
            border-collapse: collapse;
        }
        th {
            background: #409eff;
            color: white;
            padding: 15px;
            text-align: left;
            font-weight: 600;
        }
        td {
            padding: 15px;
            border-bottom: 1px solid #eee;
        }
        tr:hover {
            background-color: #f8f9fa;
        }
        .status {
            padding: 5px 10px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
            display: inline-block;
        }
        .status-active {
            background: #e6f7e6;
            color: #1a9d1a;
        }
        .status-inactive {
            background: #fff7e6;
            color: #fa8c16;
        }
        .status-pending {
            background: #e6f0ff;
            color: #1a6dcc;
        }
        .no-data {
            text-align: center;
            padding: 40px 0;
            color: #999;
            font-size: 16px;
        }
        .no-data-icon {
            font-size: 48px;
            margin-bottom: 15px;
            color: #409eff;
        }
        .footer {
            text-align: center;
            padding: 20px;
            color: #666;
            font-size: 12px;
            border-top: 1px solid #eee;
            background: #f8f9fa;
        }
        
        /* 分页样式 */
        .pagination-container {
            margin-top: 25px;
            padding: 15px 0;
            border-top: 1px solid #eee;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 15px;
        }
        .pagination-info, .pagination-controls, .pagination-jump {
            display: flex;
            align-items: center;
        }
        .pagination-info select {
            padding: 5px 10px;
            border: 1px solid #ddd;
            border-radius: 4px;
            margin: 0 5px;
        }
        .pagination-controls a {
            display: inline-block;
            padding: 5px 10px;
            margin: 0 3px;
            border: 1px solid #ddd;
            border-radius: 4px;
            text-decoration: none;
            color: #333;
            background: #f8f9fa;
            transition: all 0.2s;
        }
        .pagination-controls a:hover {
            background: #e9f0fa;
            border-color: #409eff;
            color: #409eff;
        }
        .pagination-controls a.active {
            background: #409eff;
            color: white;
            border-color: #409eff;
            font-weight: bold;
        }
        .pagination-jump input[type="text"] {
            width: 40px;
            padding: 5px;
            border: 1px solid #ddd;
            border-radius: 4px;
            margin: 0 5px;
        }
        .pagination-jump input[type="submit"] {
            padding: 5px 10px;
            background: #409eff;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }
        .pagination-jump input[type="submit"]:hover {
            background: #1a6dcc;
        }

        /* 修改部门样式 */
        .edit-form {
            display: none;
            white-space: nowrap;
        }
        .edit-toggle {
            padding: 4px 8px;
            font-size: 12px;
            background: #e6f7ff;
            border: 1px solid #91d5ff;
            border-radius: 4px;
            cursor: pointer;
            color: #1890ff;
        }
        .edit-toggle:hover {
            background: #bae7ff;
        }
        .edit-select {
            padding: 2px 5px;
            font-size: 12px;
            border: 1px solid #d9d9d9;
            border-radius: 3px;
            margin: 0 4px;
        }
        .edit-btn {
            padding: 2px 6px;
            font-size: 12px;
            margin: 0 2px;
            border: 1px solid #d9d9d9;
            border-radius: 3px;
            cursor: pointer;
        }
        .save-btn {
            background: #f6ffed;
            color: #52c41a;
            border-color: #b7eb8f;
        }
        .cancel-btn {
            background: #fff2f0;
            color: #ff4d4f;
            border-color: #ffccc7;
        }
        
        /* 模式显示样式 */
        .type-op-display {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            background-color: #f9f0ff;
            color: #722ed1;
            border: 1px solid #d3adf7;
            cursor: pointer;
            min-width: 60px;
            text-align: center;
            white-space: nowrap;
            overflow: hidden;
            text-overflow: ellipsis;
        }
        .type-op-display:hover {
            background-color: #f0e7ff;
        }
        .type-op-select {
            padding: 4px 8px;
            border: 1px solid #d9d9d9;
            border-radius: 4px;
            font-size: 12px;
            background-color: white;
            outline: none;
            cursor: pointer;
            width: 120px;
        }
        .type-op-select:focus {
            border-color: #409eff;
        }
        
        @media (max-width: 768px) {
            .stats {
                grid-template-columns: 1fr;
            }
            .header {
                flex-direction: column;
                text-align: center;
                gap: 15px;
            }
            .pagination-container {
                flex-direction: column;
                align-items: flex-start;
            }
            .pagination-controls {
                flex-wrap: wrap;
            }
        }
        
        .status-pill {
            display: inline-block;
            padding: 4px 12px;
            border-radius: 20px;
            font-size: 12px;
            font-weight: 500;
            line-height: 1.4;
            white-space: nowrap;
            text-align: center;
            min-width: 40px;
        }

        /* 收款状态 */
        .status-normal {
            background-color: #e6f7e6;
            color: #1a9d1a;
            border: 1px solid #b7eb8f;
        }
        .status-abnormal {
            background-color: #fff2f0;
            color: #ff4d4f;
            border: 1px solid #ffccc7;
        }

        /* 店铺状态 */
        .status-selling {
            background-color: #e6f7ff;
            color: #1890ff;
            border: 1px solid #91d5ff;
        }
        .status-idle {
            background-color: #f9f0ff;
            color: #722ed1;
            border: 1px solid #d3adf7;
        }
        .status-disabled {
            background-color: #f5f5f5;
            color: #8c8c8c;
            border: 1px solid #d9d9d9;
        }
        .status-closed {
            background-color: #fff1f0;
            color: #cf1322;
            border: 1px solid #ffa39e;
        }
        .status-review {
            background-color: #fffbe6;
            color: #faad14;
            border: 1px solid #ffe58f;
        }
        .status-unknown {
            background-color: #f0f0f0;
            color: #595959;
            border: 1px solid #d9d9d9;
        }

        /* 批量新增区域 */
        #bulkSection {
            display: none;
            grid-column: span 2;
            background: #f0f8ff;
            padding: 15px;
            border-radius: 6px;
            margin-top: 10px;
            border: 1px solid #bae6ff;
        }
        #bulkPreview {
            margin-top: 10px;
            font-size: 13px;
            color: #1a6dcc;
            line-height: 1.5;
        }
        #bulkPreview strong {
            color: #096dd9;
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
             <h1>账号管理系统</h1>
    <div>
        <button onclick="openAddModal()" style="
            background: #52c41a; color: white; border: none; padding: 8px 15px;
            border-radius: 4px; cursor: pointer; margin-right: 10px;
            font-size: 14px; font-weight: 500;
        ">+ 新增店铺</button>
        <a href="index.jsp" class="back-link">返回首页</a>
    </div>
        </div>
        
        <!-- 隐藏的模式数据 -->
        <div id="modeData" style="display:none;">
            <%
                List<Map<String, Object>> allModes = (List<Map<String, Object>>) request.getAttribute("allModes");
                if (allModes != null && !allModes.isEmpty()) {
            %>
                [
                <%
                    for (int i = 0; i < allModes.size(); i++) {
                        Map<String, Object> mode = allModes.get(i);
                        String typeOp = (String) mode.get("type_op");
                        Integer typeOpid = (Integer) mode.get("type_opid");
                %>
                    {"type_opid": <%= typeOpid %>, "type_op": "<%= typeOp %>"}
                    <%= i < allModes.size()-1 ? "," : "" %>
                <%
                    }
                %>
                ]
            <%
                } else {
            %>[]<%
                }
            %>
        </div>
        
        <!-- 搜索与筛选区域 -->
<div style="margin-bottom: 20px; padding: 15px; background: #f9fbfd; border-radius: 8px; border: 1px solid #e8eef5;">
    <form id="searchForm" style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 12px; align-items:end;">
        <!-- 字段选择 + 关键词输入 -->
<div style="display: grid; grid-template-columns: auto 1fr; gap: 8px; align-items: end;">
    <div>
        <label style="font-size: 13px; color: #666;">字段</label>
        <select name="searchField" id="searchField" style="width: 100%; padding: 6px 10px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 14px;">
            <option value="">-- 请选择 --</option>
            <option value="mains" <%= "mains".equals(request.getParameter("searchField")) ? "selected" : "" %>>主体简称</option>
            <option value="acc_name" <%= "acc_name".equals(request.getParameter("searchField")) ? "selected" : "" %>>店铺名称</option>
            <option value="ziniao" <%= "ziniao".equals(request.getParameter("searchField")) ? "selected" : "" %>>紫鸟</option>
            <option value="type_op" <%= "type_op".equals(request.getParameter("searchField")) ? "selected" : "" %>>模式</option>
            <option value="country" <%= "country".equals(request.getParameter("searchField")) ? "selected" : "" %>>国家</option>
            <option value="area" <%= "area".equals(request.getParameter("searchField")) ? "selected" : "" %>>区域</option>
            <option value="platform" <%= "platform".equals(request.getParameter("searchField")) ? "selected" : "" %>>平台</option>
            <option value="depart_name" <%= "depart_name".equals(request.getParameter("searchField")) ? "selected" : "" %>>销售部门</option>
        </select>
    </div>
    <div>
        <label style="font-size: 13px; color: #666;">关键词</label>
        <input type="text" name="keyword" id="keyword" 
               placeholder="请输入关键词"
               value="<%= request.getParameter("keyword") != null ? request.getParameter("keyword") : "" %>"
               style="width: 100%; padding: 6px 10px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 14px;">
    </div>
</div>


        <!-- 收款状态筛选 -->
        <div>
            <label style="font-size: 13px; color: #666;">收款状态</label>
            <select id="receiptStatus" name="receiptStatus" style="width: 100%; padding: 6px 10px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 14px;">
                <option value="">全部</option>
                <option value="1" <%= "1".equals(request.getParameter("receiptStatus")) ? "selected" : "" %>>正常</option>
                <option value="0" <%= "0".equals(request.getParameter("receiptStatus")) ? "selected" : "" %>>异常</option>
            </select>
        </div>

        <!-- 店铺状态筛选 -->
        <div>
            <label style="font-size: 13px; color: #666;">店铺状态</label>
            <select id="shopStatus" name="shopStatus" style="width: 100%; padding: 6px 10px; border: 1px solid #d9d9d9; border-radius: 4px; font-size: 14px;">
                <option value="">全部</option>
                <option value="1" <%= "1".equals(request.getParameter("shopStatus")) ? "selected" : "" %>>销售中</option>
                <option value="2" <%= "2".equals(request.getParameter("shopStatus")) ? "selected" : "" %>>闲置</option>
                <option value="3" <%= "3".equals(request.getParameter("shopStatus")) ? "selected" : "" %>>停用</option>
                <option value="4" <%= "4".equals(request.getParameter("shopStatus")) ? "selected" : "" %>>关店</option>
                <option value="5" <%= "5".equals(request.getParameter("shopStatus")) ? "selected" : "" %>>审核中</option>
                <option value="6" <%= "6".equals(request.getParameter("shopStatus")) ? "selected" : "" %>>未知</option>
            </select>
        </div>

        <!-- 操作按钮 -->
        <div style="display: flex; gap: 8px;">
            <button type="submit" style="padding: 6px 16px; background: #1890ff; color: white; border: none; border-radius: 4px; cursor: pointer;">搜索</button>
            <button type="button" onclick="clearSearch()" style="padding: 6px 16px; background: #f0f0f0; color: #666; border: 1px solid #d9d9d9; border-radius: 4px; cursor: pointer;">重置</button>
        </div>
    </form>
</div>
        
        <div class="content">
            <div class="stats">
                <div class="stat-card">
                    <div class="stat-value"><%= request.getAttribute("totalAccounts") != null ? request.getAttribute("totalAccounts") : "0" %></div>
                    <div class="stat-label">总店铺数</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value"><%= request.getAttribute("activeAccounts") != null ? request.getAttribute("activeAccounts") : "0" %></div>
                    <div class="stat-label">活跃店铺</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value"><%= request.getAttribute("platformCount") != null ? request.getAttribute("platformCount") : "0" %></div>
                    <div class="stat-label">平台类型</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value"><%= request.getAttribute("countryCount") != null ? request.getAttribute("countryCount") : "0" %></div>
                    <div class="stat-label">国家</div>
                </div>
            </div>
            
            <div class="table-container">
                <table>
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>主体简称</th>
                            <th>店铺名称</th>
                            <th>紫鸟</th>
                            <th>模式</th>
                            <th>国家</th>
                            <th>区域</th>
                            <th>平台</th>
                            <th>销售部门</th>        				
                            <th>操作</th>
                            <th>收款状态</th>
                            <th>店铺状态</th>
       						<th>操作</th>                          
                        </tr>
                    </thead>
                    <tbody>
                        <%
                            List<AccountData> accountDataList = (List<AccountData>) request.getAttribute("accountDataList");
                            List<V3> allDepartments = (List<V3>) request.getAttribute("allDepartments");
                            if (allDepartments == null) {
                                allDepartments = new java.util.ArrayList<>();
                            }
                            
                            if (accountDataList != null && !accountDataList.isEmpty()) {
                                for (AccountData account : accountDataList) {
                        %>
                        <tr data-id="<%= account.getId() %>">
                            <td><%= account.getId() %></td>
                            <td><%= account.getMains() != null ? account.getMains() : "" %></td>
<td class="acc-name-cell"><%= account.getAccName() != null ? account.getAccName() : "" %></td>
<td class="ziniao-cell"><%= account.getZiniao() != null ? account.getZiniao() : "" %></td>
                            
                            <!-- 模式列（可编辑） -->
                            <td>
                                <span class="type-op-display" 
                                      data-id="<%= account.getId() %>" 
                                      data-type-opid="<%= account.getTypeOpid() %>">
                                    <%= account.getTypeOp() != null ? account.getTypeOp() : "" %>
                                </span>
                                <select class="type-op-select" style="display:none;" data-id="<%= account.getId() %>">
                                    <option value="">--请选择--</option>
                                    <!-- 模式选项通过JS动态加载 -->
                                </select>
                            </td>
                            
                            <td><%= account.getCountryId() != 0 && account.getCountry() != null ? account.getCountry() : "" %></td>
                           <td><%= account.getArea() != null ? account.getArea() : "" %></td>
                            <td><%= account.getPlatformid() != 0 && account.getPlatform() != null ? account.getPlatform() : "" %></td>
                            <td class="sales-dept-text"><%= account.getDepartName() != null ? account.getDepartName() : "" %></td>
                            
                            <!-- 操作列 -->
                            <td>
                                <button class="edit-toggle" onclick="toggleEdit(<%= account.getId() %>)">修改部门</button>
                                <form class="edit-form" method="POST" action="UpdateSalesDepartServlet">
                                    <input type="hidden" name="accountId" value="<%= account.getId() %>">
                                    <input type="hidden" name="currentPage" value="<%= request.getAttribute("currentPage") != null ? request.getAttribute("currentPage") : "1" %>">
                                    <input type="hidden" name="pageSize" value="<%= request.getAttribute("pageSize") != null ? request.getAttribute("pageSize") : "10" %>">
                                    <select name="newSalesDepart" class="edit-select">
                                        <% for (V3 dept : allDepartments) { %>
                                            <option value="<%= dept.getSalesDepart() %>"
                                                <%= account.getSalesDepart() == dept.getSalesDepart() ? "selected" : "" %>>
                                                <%= dept.getDepartName() %>
                                            </option>
                                        <% } %>
                                    </select>
                                    <button type="submit" class="edit-btn save-btn">保存</button>
                                    <button type="button" class="edit-btn cancel-btn" onclick="toggleEdit(<%= account.getId() %>)">取消</button>
                                </form>
                            </td>
                            
                            <!-- 收款状态 -->
                            <td>
                             <%
                                String receiptStatus = account.getReceiptStatus() != null ? account.getReceiptStatus() : "1";
                                String receiptClass = "1".equals(receiptStatus) ? "status-normal" : "status-abnormal";
                            %>
                            <span class="status-pill <%= receiptClass %> receipt-status"
                                  data-id="<%= account.getId() %>" 
                                  data-status="<%= receiptStatus %>">
                                <%= "1".equals(receiptStatus) ? "正常" : "异常" %>
                            </span>
                                <select class="receipt-select" style="display:none;" data-id="<%= account.getId() %>">
                                    <option value="1" <%= "1".equals(account.getReceiptStatus()) ? "selected" : "" %>>正常</option>
                                    <option value="0" <%= "0".equals(account.getReceiptStatus()) ? "selected" : "" %>>异常</option>
                                </select>
                            </td>
                            
                            <!-- 店铺状态 -->
                            <td>
                            <%
                                String shopStatus = account.getStatus() != null ? account.getStatus() : "1";
                                String shopClass = "status-unknown";
                                String statusText = "未知";

                                if ("1".equals(shopStatus)) {
                                    shopClass = "status-selling";
                                    statusText = "销售中";
                                } else if ("2".equals(shopStatus)) {
                                    shopClass = "status-idle";
                                    statusText = "闲置";
                                } else if ("3".equals(shopStatus)) {
                                    shopClass = "status-disabled";
                                    statusText = "停用";
                                } else if ("4".equals(shopStatus)) {
                                    shopClass = "status-closed";
                                    statusText = "关店";
                                } else if ("5".equals(shopStatus)) {
                                    shopClass = "status-review";
                                    statusText = "审核中";
                                } else if ("6".equals(shopStatus)) {
                                    shopClass = "status-unknown";
                                    statusText = "未知";
                                }
                            %>
                            <span class="status-pill <%= shopClass %> shop-status"
                                  data-id="<%= account.getId() %>" 
                                  data-status="<%= shopStatus %>">
                                <%= statusText %>
                            </span>
                            <select class="status-select" style="display:none;" data-id="<%= account.getId() %>">
                                <option value="1" <%= "1".equals(account.getStatus()) ? "selected" : "" %>>销售中</option>
                                <option value="2" <%= "2".equals(account.getStatus()) ? "selected" : "" %>>闲置</option>
                                <option value="3" <%= "3".equals(account.getStatus()) ? "selected" : "" %>>停用</option>
                                <option value="4" <%= "4".equals(account.getStatus()) ? "selected" : "" %>>关店</option>
                                <option value="5" <%= "5".equals(account.getStatus()) ? "selected" : "" %>>审核中</option>
                             	<option value="6" <%= "6".equals(account.getStatus()) ? "selected" : "" %>>未知</option>
                            </select>
                            </td>
                            
                            <td>
                                <button class="edit-toggle" onclick="openSystemModal(<%= account.getId() %>)">修改系统</button>
                            </td>
                        </tr>
                        <%
                                }
                            } else {
                        %>
                        <tr>
                            <td colspan="13" class="no-data">
                                <div class="no-data-icon">🔍</div>
                                <div>没有找到账号数据</div>
                                <div style="font-size: 14px; margin-top: 10px;">系统中暂无账号数据或您没有查看权限</div>
                            </td>
                        </tr>
                        <%
                            }
                        %>
                    </tbody>
                </table>
            </div>
            
            <!-- 系统字段编辑模态框 -->
<div id="systemModal" style="display:none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center;">
    <div style="background: white; width: 90%; max-width: 800px; max-height: 90vh; overflow-y: auto; border-radius: 8px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.3);">
        <h3 style="margin-top: 0; color: #1a6dcc;">修改系统字段（账号 ID: <span id="modalAccountId"></span>）</h3>
        
        <form id="systemForm">
            <input type="hidden" id="editAccountId" name="accountId">
            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 15px;">
                <!-- 系统字段将通过 JS 动态生成 -->
            </div>
           
            <div style="text-align: right; margin-top: 20px;">
                <button type="button" onclick="closeModal()" style="padding: 8px 16px; margin-right: 10px; background: #f0f0f0; border: 1px solid #ddd; border-radius: 4px; cursor: pointer;">取消</button>
                <button type="submit" style="padding: 8px 16px; background: #52c41a; color: white; border: none; border-radius: 4px; cursor: pointer;">保存</button>
            </div>
        </form>
    </div>
</div>
            
<!-- 新增账号模态框 -->
<div id="addModal" style="display:none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); z-index: 1000; justify-content: center; align-items: center;">
    <div style="background: white; width: 90%; max-width: 700px; max-height: 90vh; overflow-y: auto; border-radius: 8px; padding: 20px; box-shadow: 0 4px 20px rgba(0,0,0,0.3);">
        <h3 style="margin-top: 0; color: #1a6dcc;">新增账号</h3>
        
        <form id="addAccountForm" style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 15px;">
            <div>
                <label style="display: block; margin-bottom: 4px; font-weight: bold;">
                    主体简称<span style="color: #ff4d4f; margin-left: 2px;">*</span>
                </label>
                <input type="text" name="mains" required style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
            </div>
            <div>
                <label style="display: block; margin-bottom: 4px; font-weight: bold;">
                    账号名称(店铺名称)<span style="color: #ff4d4f; margin-left: 2px;">*</span>
                </label>
                <input type="text" name="acc_name" required style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
            </div>
        
            <div>
                <label style="display: block; margin-bottom: 4px; font-weight: bold;">
                    紫鸟<span style="color: #ff4d4f; margin-left: 2px;">*</span>
                </label>
                <input type="text" name="ziniao" required style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
            </div>
            
            <div>
                <label style="display: block; margin-bottom: 4px; font-weight: bold;">
                    模式<span style="color: #ff4d4f; margin-left: 2px;">*</span>
                </label>
                <select name="type_opid" required style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                    <option value="">-- 请选择 --</option>
                    <!-- 将通过 AJAX 动态填充 -->
                </select>
            </div>
            
            <!-- 国家/站点和区域二选一 -->
            <div>
                <label style="display: block; margin-bottom: 4px; font-weight: bold;">国家/站点</label>
                <select name="country_id" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                    <option value="">-- 请选择 --</option>
                    <!-- 将通过 AJAX 动态填充 -->
                </select>
                <div id="country-warning" style="display: none; font-size: 12px; color: #ff4d4f; margin-top: 2px;">
                    * 国家/站点和区域至少填写一个
                </div>
            </div>
            
            <div>
                <label style="display: block; margin-bottom: 4px; font-weight: bold;">区域</label>
                <select name="area_id" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                    <option value="">-- 请选择 --</option>
                    <!-- 将通过 AJAX 动态填充 -->
                </select>
                <div id="area-warning" style="display: none; font-size: 12px; color: #ff4d4f; margin-top: 2px;">
                    * 国家/站点和区域至少填写一个
                </div>
            </div>
            
            <div>
                <label style="display: block; margin-bottom: 4px; font-weight: bold;">
                    平台<span style="color: #ff4d4f; margin-left: 2px;">*</span>
                </label>
                <select name="platformid" required style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                    <option value="">-- 请选择 --</option>
                    <!-- 将通过 AJAX 动态填充 -->
                </select>
            </div>
            <div>
                <label style="display: block; margin-bottom: 4px; font-weight: bold;">
                    销售部门<span style="color: #ff4d4f; margin-left: 2px;">*</span>
                </label>
                <select name="sales_depart" required style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                    <option value="">-- 请选择 --</option>
                    <!-- 将通过 AJAX 动态填充 -->
                </select>
            </div>        
            <!-- 新增：易仓名字段 -->
          <div style="grid-column: span 1; ">
    <label style="display: block; margin-bottom: 4px; font-weight: bold;">
        易仓名<span style="color: #ff4d4f; margin-left: 2px;">*</span>
    </label>
    <input type="text" name="s1" required     
           style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
</div>
            
            <!-- 批量新增区域（仅当 Amazon 时显示） -->
            <div id="bulkSection">
                <label>
                    <input type="checkbox" id="isBulk" onchange="toggleBulkMode()"> 批量平铺新增（按区域下的所有站点）
                </label>
                <div id="bulkAreaRow" style="margin-top: 10px; display: none;">
                    <label style="display: block; margin-bottom: 4px; font-weight: bold;">
                        选择区域（将遍历该区域下所有 Amazon 站点）<span style="color: #ff4d4f;">*</span>
                    </label>
                    <select id="bulkAreaId" style="width: 100%; padding: 8px; border: 1px solid #ddd; border-radius: 4px;">
                        <option value="">-- 请选择区域 --</option>
                        <!-- 将通过 JS 填充 -->
                    </select>
                    <div id="bulkPreview" style="margin-top: 10px;"></div>
                </div>
            </div>
            
            <div style="grid-column: span 2; text-align: right; margin-top: 10px;">
                <button type="button" onclick="closeAddModal()" style="padding: 8px 16px; margin-right: 10px; background: #f0f0f0; border: 1px solid #ddd; border-radius: 4px; cursor: pointer;">取消</button>
                <button type="submit" style="padding: 8px 16px; background: #52c41a; color: white; border: none; border-radius: 4px; cursor: pointer;">保存</button>
            </div>
        </form>
    </div>
</div>
            <!-- 分页区域 -->
            <div class="pagination-container">
                <div class="pagination-info">
                    每页显示：
                    <select name="pageSize" onchange="changePageSize(this.value)">
                        <option value="10" <%= request.getAttribute("pageSize") != null && (Integer)request.getAttribute("pageSize") == 10 ? "selected" : "" %>>10 条</option>
                        <option value="20" <%= request.getAttribute("pageSize") != null && (Integer)request.getAttribute("pageSize") == 20 ? "selected" : "" %>>20 条</option>
                        <option value="50" <%= request.getAttribute("pageSize") != null && (Integer)request.getAttribute("pageSize") == 50 ? "selected" : "" %>>50 条</option>
                        <option value="100" <%= request.getAttribute("pageSize") != null && (Integer)request.getAttribute("pageSize") == 100 ? "selected" : "" %>>100 条</option>
                        <option value="200" <%= request.getAttribute("pageSize") != null && (Integer)request.getAttribute("pageSize") == 200 ? "selected" : "" %>>200 条</option>
                    </select>
                    &nbsp;&nbsp;共 <%= request.getAttribute("totalAccounts") != null ? request.getAttribute("totalAccounts") : "0" %> 条记录，
                    <%= request.getAttribute("totalPages") != null ? request.getAttribute("totalPages") : "0" %> 页，
                    当前第 <%= request.getAttribute("currentPage") != null ? request.getAttribute("currentPage") : "1" %> 页
                </div>
                
                <div class="pagination-controls">
                    <a href="#" onclick="goToPage(1)">首页</a>
                    <a href="#" onclick="goToPage(<%= request.getAttribute("currentPage") != null && (Integer)request.getAttribute("currentPage") > 1 ? 
                        (Integer)request.getAttribute("currentPage") - 1 : 1 %>)">上一页</a>
                    
                    <%
                        Integer currentPage = (Integer) request.getAttribute("currentPage");
                        Integer totalPages = (Integer) request.getAttribute("totalPages");
                        
                        if (currentPage == null) currentPage = 1;
                        if (totalPages == null) totalPages = 1;
                        
                        int startPage = Math.max(1, currentPage - 2);
                        int endPage = Math.min(totalPages, currentPage + 2);
                        
                        for (int i = startPage; i <= endPage; i++) {
                    %>
                        <a href="#" onclick="goToPage(<%= i %>)" <%= i == currentPage ? "class='active'" : "" %>><%= i %></a>
                    <%
                        }
                    %>
                    
                    <a href="#" onclick="goToPage(<%= currentPage < totalPages ? currentPage + 1 : totalPages %>)">下一页</a>
                    <a href="#" onclick="goToPage(<%= totalPages %>)">末页</a>
                </div>
                
                <div class="pagination-jump">
                    跳至：<input type="text" id="jumpPage" size="2" value="<%= currentPage %>">
                    <input type="button" value="跳转" onclick="jumpToPage()">
                </div>
            </div>
        </div>
        
        <div class="footer">
            <p>账号管理系统 &copy; <%= new java.text.SimpleDateFormat("yyyy").format(new java.util.Date()) %> | 数据来源于 账号管理员</p>
        </div>
    </div>

    <!-- Toast 容器 -->
    <div id="toast" style="
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: #f6ffed;
        color: #52c41a;
        border: 1px solid #b7eb8f;
        padding: 12px 24px;
        border-radius: 4px;
        font-weight: bold;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 2000;
        display: none;
        opacity: 0;
        transition: opacity 0.3s ease;
    ">
        保存成功！
    </div>

<script>
//监听平台下拉框变化，控制批量区域显示
document.addEventListener('change', function(e) {
    // 精准匹配新增表单中的 platformid 下拉框
    const platformSelect = e.target.closest('#addAccountForm [name="platformid"]');
    if (platformSelect) {
        const isAmazon = platformSelect.value === '3'; // Amazon platformid = 3
        const bulkSection = document.getElementById('bulkSection');
        if (bulkSection) {
            bulkSection.style.display = isAmazon ? 'block' : 'none';
            if (!isAmazon) {
                // 非 Amazon 时重置批量选项
                const isBulkCheckbox = document.getElementById('isBulk');
                if (isBulkCheckbox) {
                    isBulkCheckbox.checked = false;
                    document.getElementById('bulkAreaRow').style.display = 'none';
                    document.getElementById('bulkPreview').textContent = '';
                }
            }
        }
    }
});
//批量新增：切换复选框
function toggleBulkMode() {
    const isChecked = document.getElementById('isBulk').checked;
    document.getElementById('bulkAreaRow').style.display = isChecked ? 'block' : 'none';
    if (!isChecked) {
        document.getElementById('bulkPreview').textContent = '';
    } else {
        // 如果已选择区域，触发预览
        const areaId = document.getElementById('bulkAreaId').value;
        if (areaId) {
            simulateBulkPreview(areaId);
        }
    }
}

// 批量新增：区域变更时预览账号名
document.getElementById('bulkAreaId').addEventListener('change', function() {
    simulateBulkPreview(this.value);
});

function simulateBulkPreview(areaId) {
    const accName = document.querySelector('#addAccountForm [name="acc_name"]')?.value?.trim() || 'test';
    const previewDiv = document.getElementById('bulkPreview');
    
    if (!areaId || !previewDiv) {
        if (previewDiv) previewDiv.textContent = '';
        return;
    }

    // 站点名称映射表（和上面保持一致）
    const siteNameMap = {
    	    'sa': '沙特阿拉伯',
    	    'ae': '阿联酋',
    	    'us': '美国',
    	    'ca': '加拿大',
    	    'mx': '墨西哥',
    	    'br': '巴西',
    	    'nl': '荷兰',
    	    'ie': '爱尔兰',
    	    'gb': '英国',
    	    'be': '比利时',
    	    'pl': '波兰',
    	    'tr': '土耳其',
    	    'se': '瑞典',
    	    'uk': '英国',
    	    'it': '意大利',
    	    'es': '西班牙',
    	    'fr': '法国',
    	    'de': '德国',
    	    'jp': '日本',
    	    'au': '澳大利亚',
    	    'in': '印度',
    	    'ph': '菲律宾',
    	    'my': '马来西亚',
    	    'id': '印度尼西亚',
    	    'tw': '中国台湾',
    	    'th': '泰国',
    	    'sg': '新加坡',
    	    'vn': '越南'
    };

    // 改为 text() 而不是 json()
    fetch('GetSitesByAreaServlet?areaId=' + encodeURIComponent(areaId) + '&platformId=3')
        .then(response => response.text()) // ← 关键：获取原始文本
        .then(text => {
            console.log("🔍 原始响应文本:", text);
            
            try {
                const sites = JSON.parse(text);
                console.log("✅ 解析后:", sites);
                console.log("类型:", typeof sites, "是否数组:", Array.isArray(sites));
                
                if (Array.isArray(sites) && sites.length > 0) {
                    // 打印第一个元素的 keys
                    console.log("第一个元素的字段:", Object.keys(sites[0]));
                    
                    const names = sites.map(site => {
                        const code = site.site || site.site_code || 'MISSING';
                        const chineseName = siteNameMap[code] || '';
                        // 拼接缩写+中文（可选）
                        const displayCode = chineseName ? `${code}（${chineseName}）` : code;
                        return `${accName}_${displayCode}`;
                    }).join('<br>');
                    
                    previewDiv.innerHTML = "<strong>将生成 " + sites.length + " 个账号：</strong><br>" + names;
                } else {
                    previewDiv.innerHTML = '<span style="color:#faad14;">⚠️ 空数组或非数组</span>';
                }
            } catch (e) {
                console.error("JSON 解析失败:", e);
                previewDiv.innerHTML = '<span style="color:#ff4d4f;">❌ JSON 格式错误</span>';
            }
        })
        .catch(err => {
            console.error("请求失败:", err);
            previewDiv.innerHTML = '<span style="color:#ff4d4f;">❌ 请求失败</span>';
        });
}
    function goToPage(page) {
        const url = new URL(window.location);
        url.searchParams.set('page', page);
        const pageSize = document.querySelector('select[name="pageSize"]').value;
        url.searchParams.set('size', pageSize);
        window.location.href = url.toString();
    }
    
    function changePageSize(size) {
        const url = new URL(window.location);
        url.searchParams.set('size', size);
        url.searchParams.set('page', 1);
        window.location.href = url.toString();
    }
    
    function jumpToPage() {
        const pageInput = document.getElementById('jumpPage');
        let page = parseInt(pageInput.value);
        const totalPages = <%= totalPages != null ? totalPages : 1 %>;
        if (isNaN(page) || page < 1) page = 1;
        else if (page > totalPages) page = totalPages;
        goToPage(page);
    }
    
    document.getElementById('jumpPage').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') jumpToPage();
    });
    
    function toggleEdit(accountId) {
        const row = document.querySelector('tr[data-id="' + accountId + '"]');
        if (!row) {
            console.error('未找到 ID 为 ' + accountId + ' 的行');
            return;
        }
        
        const textEl = row.querySelector('.sales-dept-text');
        const formEl = row.querySelector('.edit-form');
        
        if (!textEl || !formEl) {
            console.error('未找到销售部门文本或编辑表单元素');
            return;
        }

        const isHidden = window.getComputedStyle(formEl).display === 'none';
        
        if (isHidden) {
            textEl.style.display = 'none';
            formEl.style.display = 'inline-block';
        } else {
            textEl.style.display = '';
            formEl.style.display = 'none';
        }
    }

    let currentAccountId = null;

    function openSystemModal(accountId) {
        currentAccountId = accountId;
        document.getElementById('modalAccountId').textContent = accountId;
        document.getElementById('editAccountId').value = accountId;
        
        const grid = document.querySelector('#systemModal .grid');
        if (grid) grid.remove();
        const newGrid = document.createElement('div');
        newGrid.className = 'grid';
        newGrid.style.display = 'grid';
        newGrid.style.gridTemplateColumns = 'repeat(auto-fit, minmax(300px, 1fr))';
        newGrid.style.gap = '15px';
        document.getElementById('systemForm').prepend(newGrid);

        fetch('GetSystemFieldsServlet?accountId=' + accountId)
            .then(function(response) {
                return response.json();
            })
            .then(function(data) {
                var systemNames = {
                    1: "系统1（易仓）",
                    2: "系统2（积加）",
                    3: "系统3（速牛）",
                    4: "系统4（福来）",
                    5: "系统5"
                };

                for (var i = 1; i <= 20; i++) {
                    var label = systemNames[i] || ("系统" + i);
                    var fieldDiv = document.createElement('div');
                    fieldDiv.innerHTML = 
                        '<label style="display: block; margin-bottom: 4px; font-weight: bold;">' + label + '</label>' +
                        '<input type="text" name="s' + i + '" value="' + (data['s' + i] || '') + '" ' +
                        'style="width: 100%; padding: 6px; border: 1px solid #ddd; border-radius: 4px;">';
                    newGrid.appendChild(fieldDiv);
                }
                document.getElementById('systemModal').style.display = 'flex';
            })
            .catch(function(err) {
                console.error('加载系统字段失败:', err);
                alert('加载数据失败，请重试');
            });
    }

    function closeModal() {
        document.getElementById('systemModal').style.display = 'none';
    }

    document.getElementById('systemForm').addEventListener('submit', function(e) {
        e.preventDefault();
        
        var formData = new FormData(this);
        var data = {};
        for (var pair of formData.entries()) {
            data[pair[0]] = pair[1];
        }

        fetch('UpdateSystemServlet', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(function(response) {
            return response.json();
        })
        .then(function(result) {
            if (result.success) {
                showToast('保存成功！');
                closeModal();
            } else {
                alert('保存失败：' + (result.message || '未知错误'));
            }
        })
        .catch(function(err) {
            console.error('保存失败:', err);
            alert('网络错误，请重试');
        });
    });

    function showToast(message) {
        const toast = document.getElementById('toast');
        toast.textContent = message || '保存成功！';
        toast.style.display = 'block';
        setTimeout(() => {
            toast.style.opacity = '1';
        }, 10);
        setTimeout(() => {
            toast.style.opacity = '0';
            setTimeout(() => {
                toast.style.display = 'none';
            }, 300);
        }, 2000);
    }

    // 打开新增模态框
    function openAddModal() {
        document.getElementById('addModal').style.display = 'flex';
        if (!window.dropdownsLoaded) {
            loadDropdownOptions();
        }
    }

    function closeAddModal() {
        document.getElementById('addModal').style.display = 'none';
        // 重置批量区域
        document.getElementById('isBulk').checked = false;
        document.getElementById('bulkAreaRow').style.display = 'none';
        document.getElementById('bulkPreview').textContent = '';
    }
 // 加载所有下拉选项
    function loadDropdownOptions() {
        // ===== 完整站点缩写-中文映射表（适配你提供的所有站点）=====
        const siteNameMap = {
            'sa': '沙特阿拉伯',
            'ae': '阿联酋',
            'us': '美国',
            'ca': '加拿大',
            'mx': '墨西哥',
            'br': '巴西',
            'nl': '荷兰',
            'ie': '爱尔兰',
            'gb': '英国(大不列颠)',
            'be': '比利时',
            'pl': '波兰',
            'tr': '土耳其',
            'se': '瑞典',
            'uk': '英国',
            'it': '意大利',
            'es': '西班牙',
            'fr': '法国',
            'de': '德国',
            'jp': '日本',
            'au': '澳大利亚',
            'in': '印度',
            'ph': '菲律宾',
            'my': '马来西亚',
            'id': '印度尼西亚',
            'tw': '中国台湾',
            'th': '泰国',
            'sg': '新加坡',
            'vn': '越南'
        };

        Promise.all([
            fetch('GetModesServlet').then(r => r.json()),
            fetch('GetSitesServlet').then(r => r.json()),
            fetch('GetAreasServlet').then(r => r.json()),
            fetch('GetPlatformsServlet').then(r => r.json()),
            fetch('GetSalesDepartmentsServlet').then(r => r.json())
        ]).then(([modes, sites, areas, platforms, depts]) => {
            const modeSelect = document.querySelector('#addAccountForm [name="type_opid"]');
            modeSelect.innerHTML = '<option value="">-- 请选择 --</option>';
            modes.forEach(item => {
                const opt = document.createElement('option');
                opt.value = item.type_opid;
                opt.textContent = item.type_op;
                modeSelect.appendChild(opt);
            });

            // ===== 站点下拉框拼接中文名称（无遗漏）=====
            const siteSelect = document.querySelector('#addAccountForm [name="country_id"]');
            siteSelect.innerHTML = '<option value="">-- 请选择 --</option>';
            sites.forEach(item => {
                const opt = document.createElement('option');
                opt.value = item.site_id;
                // 核心逻辑：缩写+中文，确保你给的所有站点都能显示对应中文
                const chineseName = siteNameMap[item.site] || '';
                opt.textContent = chineseName ? `${item.site}（${chineseName}）` : item.site;
                siteSelect.appendChild(opt);
            });

            const areaSelect = document.querySelector('#addAccountForm [name="area_id"]');
            areaSelect.innerHTML = '<option value="">-- 请选择 --</option>';
            areas.forEach(item => {
                const opt = document.createElement('option');
                opt.value = item.area_id;
                opt.textContent = item.area;
                areaSelect.appendChild(opt);
            });

            // 填充批量区域下拉
            const bulkAreaSelect = document.getElementById('bulkAreaId');
            bulkAreaSelect.innerHTML = '<option value="">-- 请选择区域 --</option>';
            areas.forEach(item => {
                const opt = document.createElement('option');
                opt.value = item.area_id;
                opt.textContent = item.area;
                bulkAreaSelect.appendChild(opt);
            });

            const platSelect = document.querySelector('#addAccountForm [name="platformid"]');
            platSelect.innerHTML = '<option value="">-- 请选择 --</option>';
            platforms.forEach(item => {
                const opt = document.createElement('option');
                opt.value = item.platformid;
                opt.textContent = item.platform;
                platSelect.appendChild(opt);
            });

            const deptSelect = document.querySelector('#addAccountForm [name="sales_depart"]');
            deptSelect.innerHTML = '<option value="">-- 请选择 --</option>';
            depts.forEach(item => {
                const opt = document.createElement('option');
                opt.value = item.sales_depart;
                opt.textContent = item.depart_name;
                deptSelect.appendChild(opt);
            });

            window.dropdownsLoaded = true;
            window.allAreas = areas; // 保存用于后续
        }).catch(err => {
            console.error('加载下拉选项失败:', err);
            alert('加载选项失败，请刷新页面重试');
        });
    }

 // 处理新增表单提交
   // 处理新增表单提交（支持单条 + 批量）
document.getElementById('addAccountForm').addEventListener('submit', function(e) {
    e.preventDefault();
    const isBulk = document.getElementById('isBulk')?.checked || false;
    const platformId = document.querySelector('#addAccountForm [name="platformid"]').value;
    const isAmazon = platformId === '3';

    if (isAmazon && isBulk) {
        // ========== 批量新增逻辑 ==========
        const bulkAreaId = document.getElementById('bulkAreaId').value;
        const accName = document.querySelector('[name="acc_name"]').value.trim();
        const mains = document.querySelector('[name="mains"]').value.trim();
        const ziniao = document.querySelector('[name="ziniao"]').value.trim();
        const typeOpid = document.querySelector('[name="type_opid"]').value;
        const salesDepart = document.querySelector('[name="sales_depart"]').value;
        const s1 = document.querySelector('[name="s1"]').value.trim();

        let hasError = false;
        if (!accName) { alert('请输入基础账号名称'); hasError = true; }
        if (!mains) { alert('主体简称不能为空'); hasError = true; }
        if (!ziniao) { alert('紫鸟不能为空'); hasError = true; }
        if (!typeOpid) { alert('请选择模式'); hasError = true; }
        if (!salesDepart) { alert('请选择销售部门'); hasError = true; }
        if (!s1) { alert('易仓名不能为空'); hasError = true; }
        if (!bulkAreaId) { alert('请选择区域'); hasError = true; }

        if (hasError) return;

        // 获取该区域下所有 Amazon 站点
        fetch('GetSitesByAreaServlet?areaId=' + encodeURIComponent(bulkAreaId) + '&platformId=3')
            .then(r => r.json())
            .then(sites => {
                if (!sites || sites.length === 0) {
                    alert('该区域下没有 Amazon 站点');
                    return;
                }

                const accounts = sites.map(site => ({
                    mains: mains,
                    acc_name: accName + '_' + site.site,
                    ziniao: ziniao,
                    type_opid: parseInt(typeOpid),
                    country_id: site.site_id, // 注意：存的是 site_id
                    area_id: parseInt(bulkAreaId),
                    platformid: 3,
                    sales_depart: parseInt(salesDepart),
                    status: '1',
                    s1: s1
                }));

                return fetch('BatchAddAccountServlet', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ accounts: accounts })
                }).then(res => res.json());
            })
            .then(result => {
                if (result && result.success) {
                    showToast('批量新增成功！');
                    closeAddModal();
                    setTimeout(() => window.location.reload(), 1000);
                } else {
                    alert('批量新增失败：' + (result?.message || '未知错误'));
                }
            })
            .catch(err => {
                console.error('批量新增失败:', err);
                alert('网络错误，请重试');
            });

    } else {
        // ========== 原有单条新增逻辑 ==========
        const formData = new FormData(this);
        const data = {};
        let hasError = false;
        
        const mains = formData.get('mains');
        const accName = formData.get('acc_name');
        const s1 = formData.get('s1');
        const typeOpid = formData.get('type_opid');
        const platformid = formData.get('platformid');
        const salesDepart = formData.get('sales_depart');
        const countryId = formData.get('country_id');
        const areaId = formData.get('area_id');
        
        document.getElementById('country-warning').style.display = 'none';
        document.getElementById('area-warning').style.display = 'none';
        
        if (!mains || mains.trim() === '') { alert('主体简称不能为空'); hasError = true; }
        if (!accName || accName.trim() === '') { alert('店铺名称不能为空'); hasError = true; }
        if (!s1 || s1.trim() === '') { alert('易仓名不能为空'); hasError = true; }
        if (!typeOpid || typeOpid === '') { alert('请选择模式'); hasError = true; }
        if (!platformid || platformid === '') { alert('请选择平台'); hasError = true; }
        if (!salesDepart || salesDepart === '') { alert('请选择销售部门'); hasError = true; }
        if ((!countryId || countryId === '') && (!areaId || areaId === '')) {
            document.getElementById('country-warning').style.display = 'block';
            document.getElementById('area-warning').style.display = 'block';
            hasError = true;
        }
        
        if (hasError) return;
        
        for (let [key, value] of formData.entries()) {
            data[key] = value;
        }
        if (!data['country_id']) data['country_id'] = '';
        if (!data['area_id']) data['area_id'] = '';
        
        fetch('AddAccountServlet', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        })
        .then(response => response.json())
        .then(result => {
            if (result.success) {
                showToast('新增成功！');
                closeAddModal();
                setTimeout(() => window.location.reload(), 1000);
            } else {
                alert('新增失败：' + (result.message || '未知错误'));
            }
        })
        .catch(err => {
            console.error('新增失败:', err);
            alert('网络错误，请重试');
        });
    }
});

    // ✅ 使用事件委托处理状态点击
    document.addEventListener('click', function(e) {
        // 收款状态点击
        if (e.target.classList.contains('receipt-status')) {
            var id = e.target.getAttribute('data-id');
            e.target.style.display = 'none';
            var select = document.querySelector('.receipt-select[data-id="' + id + '"]');
            if (select) select.style.display = 'inline-block';
        }
        // 店铺状态点击
        else if (e.target.classList.contains('shop-status')) {
            var id = e.target.getAttribute('data-id');
            e.target.style.display = 'none';
            var select = document.querySelector('.status-select[data-id="' + id + '"]');
            if (select) select.style.display = 'inline-block';
        }
        // 模式显示文本点击
        else if (e.target.classList.contains('type-op-display')) {
            var id = e.target.getAttribute('data-id');
            e.target.style.display = 'none';
            var select = document.querySelector('.type-op-select[data-id="' + id + '"]');
            if (select) {
                // 如果选项还没加载，先加载
                if (select.children.length <= 1) {
                    loadModeOptions(select);
                }
                select.style.display = 'inline-block';
            }
        }
    });
    // ✅ 收款状态切换
    document.addEventListener('change', function(e) {
        if (e.target.classList.contains('receipt-select')) {
            var id = e.target.getAttribute('data-id');
            var newStatus = e.target.value;
            
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'UpdateReceiptStatusServlet', true);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    var result = JSON.parse(xhr.responseText);
                    if (result.success) {
                        showToast('收款状态已更新');
                        var span = document.querySelector('.receipt-status[data-id="' + id + '"]');
                        var select = document.querySelector('.receipt-select[data-id="' + id + '"]');
                        if (span) {
                            const text = (newStatus === '1') ? '正常' : '异常';
                            const newClass = (newStatus === '1') ? 'status-pill status-normal receipt-status' : 'status-pill status-abnormal receipt-status';
                            span.textContent = text;
                            span.className = newClass;
                            span.setAttribute('data-status', newStatus);
                            span.style.display = 'inline';
                        }
                        if (select) select.style.display = 'none';
                    } else {
                        alert('更新失败: ' + (result.message || '未知错误'));
                    }
                }
            };
            xhr.send(JSON.stringify({ accountId: id, receiptStatus: newStatus }));
        }
    });
    // ✅ 店铺状态切换
    document.addEventListener('change', function(e) {
        if (e.target.classList.contains('status-select')) {
            var id = e.target.getAttribute('data-id');
            var newStatus = e.target.value;
            
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'UpdateShopStatusServlet', true);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    var result = JSON.parse(xhr.responseText);
                    if (result.success) {
                        showToast('店铺状态已更新');
                        // 更新状态文本
                        let statusText = '未知';
                        if (newStatus === '1') statusText = '销售中';
                        else if (newStatus === '2') statusText = '闲置';
                        else if (newStatus === '3') statusText = '停用';
                        else if (newStatus === '4') statusText = '关店';
                        else if (newStatus === '5') statusText = '审核中';
                        // newStatus === '6' 保持 "未知"

                        // 更新 CSS 类
                        let newClass = 'status-pill ';
                        if (newStatus === '1') newClass += 'status-selling';
                        else if (newStatus === '2') newClass += 'status-idle';
                        else if (newStatus === '3') newClass += 'status-disabled';
                        else if (newStatus === '4') newClass += 'status-closed';
                        else if (newStatus === '5') newClass += 'status-review';
                        else newClass += 'status-unknown';
                        newClass += ' shop-status';

                        var span = document.querySelector('.shop-status[data-id="' + id + '"]');
                        var select = document.querySelector('.status-select[data-id="' + id + '"]');
                        if (span) {
                            span.textContent = statusText;
                            span.className = newClass;
                            span.setAttribute('data-status', newStatus);
                            span.style.display = 'inline';
                        }
                        if (select) select.style.display = 'none';
                    } else {
                        alert('更新失败: ' + (result.message || '未知错误'));
                    }
                }
            };
            xhr.send(JSON.stringify({ accountId: id, status: newStatus }));
        }
    });
    // ✅ 模式下拉框改变事件
    document.addEventListener('change', function(e) {
        if (e.target.classList.contains('type-op-select')) {
            var id = e.target.getAttribute('data-id');
            var newTypeOpid = e.target.value;
            
            if (!newTypeOpid) {
                // 如果选择了"--请选择--"，恢复显示
                var span = document.querySelector('.type-op-display[data-id="' + id + '"]');
                if (span) {
                    span.style.display = 'inline';
                }
                e.target.style.display = 'none';
                return;
            }
            
            // 发送AJAX请求更新模式
            var xhr = new XMLHttpRequest();
            xhr.open('POST', 'UpdateTypeOpServlet', true);
            xhr.setRequestHeader('Content-Type', 'application/json');
            xhr.onreadystatechange = function() {
                if (xhr.readyState === 4 && xhr.status === 200) {
                    var result = JSON.parse(xhr.responseText);
                    if (result.success) {
                        showToast('模式已更新');
                        // 更新显示文本
                        var span = document.querySelector('.type-op-display[data-id="' + id + '"]');
                        var select = document.querySelector('.type-op-select[data-id="' + id + '"]');
                        if (span) {
                            // 从下拉框中获取选中的文本
                            var selectedOption = select.options[select.selectedIndex];
                            span.textContent = selectedOption.text;
                            span.setAttribute('data-type-opid', newTypeOpid);
                            span.style.display = 'inline';
                        }
                        if (select) select.style.display = 'none';
                    } else {
                        alert('更新失败: ' + (result.message || '未知错误'));
                    }
                }
            };
            xhr.send(JSON.stringify({ accountId: id, typeOpid: newTypeOpid }));
        }
    });
    
 // ✅ 点击页面任意位置，如果不在状态标签或下拉框内，则关闭所有下拉框
    document.addEventListener('click', function(e) {
        const receiptStatus = e.target.closest('.receipt-status, .receipt-select');
        const shopStatus = e.target.closest('.shop-status, .status-select');
        const typeOpStatus = e.target.closest('.type-op-display, .type-op-select');
        const bulkSection = e.target.closest('#bulkSection');
        
        // 如果点击的不是收款状态相关元素
        if (!receiptStatus) {
            document.querySelectorAll('.receipt-select').forEach(select => {
                if (select.style.display !== 'none') {
                    const id = select.getAttribute('data-id');
                    const span = document.querySelector('.receipt-status[data-id="' + id + '"]');
                    if (span) {
                        span.style.display = 'inline';
                        select.style.display = 'none';
                    }
                }
            });
        }

        // 如果点击的不是店铺状态相关元素
        if (!shopStatus) {
            document.querySelectorAll('.status-select').forEach(select => {
                if (select.style.display !== 'none') {
                    const id = select.getAttribute('data-id');
                    const span = document.querySelector('.shop-status[data-id="' + id + '"]');
                    if (span) {
                        span.style.display = 'inline';
                        select.style.display = 'none';
                    }
                }
            });
        }
        
        // 如果点击的不是模式相关元素
        if (!typeOpStatus) {
            document.querySelectorAll('.type-op-select').forEach(select => {
                if (select.style.display !== 'none') {
                    const id = select.getAttribute('data-id');
                    const span = document.querySelector('.type-op-display[data-id="' + id + '"]');
                    if (span) {
                        span.style.display = 'inline';
                        select.style.display = 'none';
                    }
                }
            });
        }
    });  
    // 加载模式选项到下拉框
    function loadModeOptions(selectElement) {
        const modeData = JSON.parse(document.getElementById('modeData').textContent);
        const currentId = selectElement.getAttribute('data-id');
        
        // 清空现有选项（除了第一个"--请选择--"）
        while (selectElement.children.length > 1) {
            selectElement.removeChild(selectElement.lastChild);
        }        
        modeData.forEach(mode => {
            const option = document.createElement('option');
            option.value = mode.type_opid;
            option.textContent = mode.type_op;
            // 设置选中当前模式
            const displaySpan = document.querySelector('.type-op-display[data-id="' + currentId + '"]');
            if (displaySpan && displaySpan.getAttribute('data-type-opid') == mode.type_opid) {
                option.selected = true;
            }
            selectElement.appendChild(option);
        });
    }    
    // 初始化模式下拉框（预加载）
    function preloadModeOptions() {
        document.querySelectorAll('.type-op-select').forEach(select => {
            loadModeOptions(select);
        });
    }
    // 页面加载完成后预加载模式选项
    window.addEventListener('load', function() {
        setTimeout(preloadModeOptions, 500);
    });
 // 提交搜索表单（带分页重置）
    document.getElementById('searchForm').addEventListener('submit', function(e) {
        e.preventDefault();        
        const formData = new FormData(this);
        const params = new URLSearchParams();       
        // 添加搜索和筛选参数
        for (let [key, value] of formData.entries()) {
            if (value.trim() !== '') {
                params.append(key, value.trim());
            }
        }      
        // 重置分页到第一页
        params.set('page', '1');     
        // 保留 pageSize（如果已设置）
        const currentSize = document.querySelector('select[name="pageSize"]').value;
        if (currentSize) {
            params.set('size', currentSize);
        }       
        const url = new URL(window.location);
        url.search = params.toString();
        window.location.href = url.toString();
    });
    // 重置搜索
    function clearSearch() {
        const url = new URL(window.location);
        url.search = ''; // 清空
        window.location.href = url.toString();
    }
 // 自动应用筛选（当任一下拉框变化时）
    document.getElementById('receiptStatus').addEventListener('change', autoApplyFilters);
    document.getElementById('shopStatus').addEventListener('change', autoApplyFilters);
    function autoApplyFilters() {
        const form = document.getElementById('searchForm');      
        const formData = new FormData(form);
        const params = new URLSearchParams();
        
        // 添加所有非空参数
        for (let [key, value] of formData.entries()) {
            if (value.trim() !== '') {
                params.append(key, value.trim());
            }
        }       
        // 保留当前分页大小（size），但重置到第一页
        const currentSize = document.querySelector('select[name="pageSize"]')?.value || '10';
        params.set('page', '1');
        params.set('size', currentSize);
        
        // 跳转
        const url = new URL(window.location);
        url.search = params.toString();
        window.location.href = url.toString();
    }
</script>
    <!-- ✅ 在这里插入 Toast 容器 -->
    <div id="toast" style="
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        background: #f6ffed;
        color: #52c41a;
        border: 1px solid #b7eb8f;
        padding: 12px 24px;
        border-radius: 4px;
        font-weight: bold;
        box-shadow: 0 4px 12px rgba(0,0,0,0.15);
        z-index: 2000;
        display: none;
        opacity: 0;
        transition: opacity 0.3s ease;
    ">
        保存成功！
    </div>
</body>
</html>