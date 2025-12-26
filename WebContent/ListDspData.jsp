<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="com.model.Dsp" %>
<%@ page import="java.text.DecimalFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.Date" %>
<%@ page import="com.model.User" %>
<%@ page import="com.util.PageUtil" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>汇率数据表</title>
    <style>
 body { 
    font-family: Arial, sans-serif; 
    max-width: 1200px; 
    margin: 0 auto; 
    padding: 20px; 
    background: #f5f5f5; 
}
.container { 
    background: white; 
    padding: 30px; 
    border-radius: 8px; 
    box-shadow: 0 2px 10px rgba(0,0,0,0.1); 
}
.user-info { 
    text-align: right; 
    margin-bottom: 20px; 
    color: #666; 
}
.user-info a { 
    color: #f56c6c; 
    text-decoration: none; 
    margin-left: 10px; 
}
.title-container {
    position: relative;
    margin-bottom: 25px;
    padding-top: 40px;
}
.back-to-home {
    position: absolute;
    top: 0;
    left: 0;
}
.back-to-home .btn {
    background: none;
    border: none;
    border-radius: 0;
    padding: 0;
    font-size: 18px;
    color: #409eff;
    text-decoration: underline;
    cursor: pointer;
    font-family: Arial, sans-serif;
    font-weight: 500;
}
.back-to-home .btn:hover {
    background: none;
    color: #3390e0;
    text-decoration: none;
}
h1 { 
    color: #333; 
    padding-bottom: 10px; 
    border-bottom: 2px solid #409eff; 
    margin: 0;
}
.form-group { 
    margin-bottom: 20px; 
    position: relative;
}
label { 
    display: block; 
    margin-bottom: 8px; 
    font-weight: bold; 
    color: #666; 
}
input, select { 
    padding: 10px; 
    border: 1px solid #ddd; 
    border-radius: 4px; 
    box-sizing: border-box; 
}
.btn { 
    padding: 10px 20px; 
    background: #409eff; 
    color: white; 
    border: none; 
    border-radius: 4px; 
    cursor: pointer; 
    font-size: 16px; 
}
.btn:hover { 
    background: #3390e0; 
}
.table-container { 
    overflow-x: auto; 
}
table { 
    width: 100%; 
    border-collapse: collapse; 
    margin-top: 20px; 
}
th, td { 
    border: 1px solid #ddd; 
    padding: 12px; 
    text-align: left; 
}
th { 
    background-color: #f8f9fa; 
}
tr:hover { 
    background-color: #f5f5f5; 
}
.pagination { 
    margin-top: 20px; 
    text-align: center; 
    display: flex;
    justify-content: center;
    align-items: center;
    flex-wrap: wrap;
    gap: 5px;
}
.pagination a, .pagination span {
    display: inline-block;
    padding: 6px 12px;
    margin: 0 3px;
    border: 1px solid #ddd;
    border-radius: 4px;
    text-decoration: none;
    color: #333;
    font-size: 14px;
    transition: all 0.3s;
}
.pagination a:hover {
    background: #e9ecef;
}
.pagination .active {
    background: #409eff;
    color: white;
    border-color: #409eff;
    font-weight: bold;
}
.pagination .disabled {
    color: #ccc;
    cursor: not-allowed;
}
.pagination-info {
    margin-right: 10px;
    font-size: 14px;
    color: #666;
}
.search-form { 
    display: flex; 
    flex-wrap: wrap; 
    gap: 15px; 
    margin-bottom: 20px; 
    align-items: flex-end;
}
.search-form .form-group { 
    margin: 0; 
}
.currency-selector {
    width: 150px;
}
.month-selector {
    width: 150px;
}
.search-row {
    display: flex;
    gap: 15px;
    margin-bottom: 20px;
}
.currency-list {
    display: none;
    position: absolute;
    z-index: 1000;
    background: white;
    border: 1px solid #ddd;
    border-radius: 4px;
    max-height: 200px;
    overflow-y: auto;
    width: 100%;
    box-shadow: 0 2px 10px rgba(0,0,0,0.1);
    margin-top: 5px;
}
.currency-item {
    padding: 5px 10px;
    background: #f0f7ff;
    border: 1px solid #ddd;
    border-radius: 4px;
    cursor: pointer;
    margin-bottom: 3px;
}
.currency-item:hover {
    background: #e6f7ff;
}
.currency-item.active {
    background: #409eff;
    color: white;
}
.search-container {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    align-items: flex-end;
}
.search-controls {
    display: flex;
    gap: 10px;
}
.search-controls .btn {
    margin-top: 0;
}
.form-group {
    margin: 0;
}
    </style>
</head>
<body>
    <div class="container">
        <div class="user-info">
            <% 
                User loginUser = (User) request.getSession().getAttribute("loginUser");
                if (loginUser != null) {
            %>
                欢迎您，<%= loginUser.getUsername() %>！
                <a href="LogoutServlet">退出登录</a>
            <% } %>
        </div>

        <div class="title-container">
            <div class="back-to-home">
                <a href="index.jsp" class="btn">返回首页</a>
            </div>
            <h1>汇率数据表</h1>
  <p style="text-align: left; font-size: 12px; color: #999; margin: 10px 0 0; cursor: pointer;">
  <a href="DownloadCenterUserServlet" style="color: inherit; text-decoration: none;">去往下载中心导出 →</a>
</p>
        </div>

        <form id="searchForm" action="ListDspDataServlet" method="get">
            <div class="search-container">
                <div class="form-group">
                    <label>币种搜索</label>
                    <input type="text" name="currency" value="<%= request.getParameter("currency") != null ? request.getParameter("currency") : "" %>" 
                           placeholder="输入币种代码" 
                           class="currency-selector" 
                           id="currencyInput"
                           onfocus="showCurrencyList()">
                    <div class="currency-list" id="currencyList">
                        <div class="currency-item" onclick="setCurrency('USD')">USD (美元)</div>
                        <div class="currency-item" onclick="setCurrency('CNY')">CNY (人民币)</div>
                        <div class="currency-item" onclick="setCurrency('EUR')">EUR (欧元)</div>
                        <div class="currency-item" onclick="setCurrency('GBP')">GBP (英镑)</div>
                        <div class="currency-item" onclick="setCurrency('JPY')">JPY (日元)</div>
                        <div class="currency-item" onclick="setCurrency('HKD')">HKD (港元)</div>
                        <div class="currency-item" onclick="setCurrency('SGD')">SGD (新加坡元)</div>
                        <div class="currency-item" onclick="setCurrency('AUD')">AUD (澳元)</div>
                        <div class="currency-item" onclick="setCurrency('CAD')">CAD (加元)</div>
                        <div class="currency-item" onclick="setCurrency('INR')">INR (印度卢比)</div>
                        <div class="currency-item" onclick="setCurrency('BRL')">BRL (巴西雷亚尔)</div>
                        <div class="currency-item" onclick="setCurrency('RUB')">RUB (俄罗斯卢布)</div>
                        <div class="currency-item" onclick="setCurrency('TRY')">TRY (土耳其里拉)</div>
                        <div class="currency-item" onclick="setCurrency('MXN')">MXN (墨西哥比索)</div>
                        <div class="currency-item" onclick="setCurrency('VND')">VND (越南盾)</div>
                        <div class="currency-item" onclick="setCurrency('THB')">THB (泰铢)</div>
                        <div class="currency-item" onclick="setCurrency('PHP')">PHP (菲律宾比索)</div>
                        <div class="currency-item" onclick="setCurrency('IDR')">IDR (印尼盾)</div>
                        <div class="currency-item" onclick="setCurrency('MYR')">MYR (马来西亚林吉特)</div>
                        <div class="currency-item" onclick="setCurrency('NOK')">NOK (挪威克朗)</div>
                        <div class="currency-item" onclick="setCurrency('DKK')">DKK (丹麦克朗)</div>
                        <div class="currency-item" onclick="setCurrency('PLN')">PLN (波兰兹罗提)</div>
                        <div class="currency-item" onclick="setCurrency('TWD')">TWD (新台币)</div>
                        <div class="currency-item" onclick="setCurrency('COP')">COP (哥伦比亚比索)</div>
                        <div class="currency-item" onclick="setCurrency('CLP')">CLP (智利比索)</div>
                        <div class="currency-item" onclick="setCurrency('ANG')">ANG (荷属安的列斯盾)</div>
                        <div class="currency-item" onclick="setCurrency('RMB')">RMB (人民币)</div>
                    </div>
                </div>
                
                <div class="form-group">
                    <label>选择月份</label>
                    <input type="month" name="month" value="<%= request.getParameter("month") != null ? request.getParameter("month") : "" %>" 
                           placeholder="选择月份" class="month-selector">
                </div>
                
                <div class="search-controls">
                    <button type="submit" class="btn">搜索</button>
                    <button type="button" class="btn" onclick="resetForm()">重置</button>
                </div>
            </div>
        </form>

        <div class="table-container">
            <table>
                <thead>
                    <tr>
                        <th>月份</th>
                        <th>币种</th>
                        <th>汇率</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        List<Dsp> dspList = (List<Dsp>) request.getAttribute("dspList");
                        if (dspList != null && !dspList.isEmpty()) {
                            SimpleDateFormat yearMonthFormat = new SimpleDateFormat("yyyy年MM月");
                            DecimalFormat rateFormat = new DecimalFormat("0.0000");
                            
                            for (Dsp dsp : dspList) {
                                String currencyCode = dsp.getCurrency();
                                String currencyName = "";
                                
                                if ("USD".equals(currencyCode)) currencyName = "美元";
                                else if ("CNY".equals(currencyCode) || "RMB".equals(currencyCode)) currencyName = "人民币";
                                else if ("EUR".equals(currencyCode)) currencyName = "欧元";
                                else if ("GBP".equals(currencyCode)) currencyName = "英镑";
                                else if ("JPY".equals(currencyCode)) currencyName = "日元";
                                else if ("HKD".equals(currencyCode)) currencyName = "港元";
                                else if ("SGD".equals(currencyCode)) currencyName = "新加坡元";
                                else if ("AUD".equals(currencyCode)) currencyName = "澳元";
                                else if ("CAD".equals(currencyCode)) currencyName = "加元";
                                else if ("CHF".equals(currencyCode)) currencyName = "瑞士法郎";
                                else if ("SEK".equals(currencyCode)) currencyName = "瑞典克朗";
                                else if ("NZD".equals(currencyCode)) currencyName = "新西兰元";
                                else if ("KRW".equals(currencyCode)) currencyName = "韩元";
                                else if ("INR".equals(currencyCode)) currencyName = "印度卢比";
                                else if ("BRL".equals(currencyCode)) currencyName = "巴西雷亚尔";
                                else if ("RUB".equals(currencyCode)) currencyName = "俄罗斯卢布";
                                else if ("TRY".equals(currencyCode)) currencyName = "土耳其里拉";
                                else if ("MXN".equals(currencyCode)) currencyName = "墨西哥比索";
                                else if ("ZAR".equals(currencyCode)) currencyName = "南非兰特";
                                else if ("VND".equals(currencyCode)) currencyName = "越南盾";
                                else if ("THB".equals(currencyCode)) currencyName = "泰铢";
                                else if ("PHP".equals(currencyCode)) currencyName = "菲律宾比索";
                                else if ("IDR".equals(currencyCode)) currencyName = "印尼盾";
                                else if ("MYR".equals(currencyCode)) currencyName = "马来西亚林吉特";
                                else if ("NOK".equals(currencyCode)) currencyName = "挪威克朗";
                                else if ("DKK".equals(currencyCode)) currencyName = "丹麦克朗";
                                else if ("PLN".equals(currencyCode)) currencyName = "波兰兹罗提";
                                else if ("TWD".equals(currencyCode)) currencyName = "新台币";
                                else if ("COP".equals(currencyCode)) currencyName = "哥伦比亚比索";
                                else if ("CLP".equals(currencyCode)) currencyName = "智利比索";
                                else if ("ANG".equals(currencyCode)) currencyName = "荷属安的列斯盾";
                    %>
                        <tr>
                            <td><%= yearMonthFormat.format(dsp.getDayStart()) %></td>
                            <td><%= currencyCode %> (<%= currencyName %>)</td>
                            <td><%= rateFormat.format(dsp.getRate()) %></td>
                        </tr>
                    <%
                            }
                        } else {
                    %>
                        <tr>
                            <td colspan="3" style="text-align: center;">暂无数据</td>
                        </tr>
                    <%
                        }
                    %>
                </tbody>
            </table>
        </div>

        <div class="pagination">
            <%
                PageUtil pageUtil = (PageUtil) request.getAttribute("pageUtil");
                if (pageUtil != null) {
                    int currentPage = pageUtil.getCurrentPage();
                    int totalPages = pageUtil.getTotalPages();
                    String currency = (String) request.getAttribute("currency");
                    String month = (String) request.getAttribute("month");
                    int size = (Integer) request.getAttribute("size");
                    
                    int total = pageUtil.getTotal();
                    int startRecord = (currentPage - 1) * size + 1;
                    int endRecord = Math.min(currentPage * size, total);
            %>
            <span class="pagination-info">共 <%= total %> 条记录，每页 <%= size %> 条，当前第 <%= currentPage %> 页 / 共 <%= totalPages %> 页</span>
            
            <%
                if (currentPage > 1) {
                    String url = "ListDspDataServlet?page=1&size=" + size;
                    if (currency != null && !currency.isEmpty()) {
                        url += "&currency=" + currency;
                    }
                    if (month != null && !month.isEmpty()) {
                        url += "&month=" + month;
                    }
            %>
                <a href="<%= url %>">首页</a>
            <%
                } else {
            %>
                <span class="disabled">首页</span>
            <%
                }
            %>
            
            <%
                if (currentPage > 1) {
                    String url = "ListDspDataServlet?page=" + (currentPage - 1) + "&size=" + size;
                    if (currency != null && !currency.isEmpty()) {
                        url += "&currency=" + currency;
                    }
                    if (month != null && !month.isEmpty()) {
                        url += "&month=" + month;
                    }
            %>
                <a href="<%= url %>">上一页</a>
            <%
                } else {
            %>
                <span class="disabled">上一页</span>
            <%
                }
            %>
            
            <%
                int startPage = Math.max(1, currentPage - 2);
                int endPage = Math.min(totalPages, currentPage + 2);
                
                if (totalPages > 5 && currentPage > 3) {
                    startPage = currentPage - 2;
                    endPage = currentPage + 2;
                    if (endPage > totalPages) {
                        endPage = totalPages;
                        startPage = totalPages - 4;
                    }
                }
                
                if (startPage > 1) {
            %>
                <span>...</span>
            <%
                }
                
                for (int i = startPage; i <= endPage; i++) {
                    String url = "ListDspDataServlet?page=" + i + "&size=" + size;
                    if (currency != null && !currency.isEmpty()) {
                        url += "&currency=" + currency;
                    }
                    if (month != null && !month.isEmpty()) {
                        url += "&month=" + month;
                    }
                    
                    if (i == currentPage) {
            %>
                        <span class="active"><%= i %></span>
            <%
                    } else {
            %>
                        <a href="<%= url %>"><%= i %></a>
            <%
                    }
                }
                
                if (endPage < totalPages) {
            %>
                <span>...</span>
            <%
                }
            %>
            
            <%
                if (currentPage < totalPages) {
                    String url = "ListDspDataServlet?page=" + (currentPage + 1) + "&size=" + size;
                    if (currency != null && !currency.isEmpty()) {
                        url += "&currency=" + currency;
                    }
                    if (month != null && !month.isEmpty()) {
                        url += "&month=" + month;
                    }
            %>
                <a href="<%= url %>">下一页</a>
            <%
                } else {
            %>
                <span class="disabled">下一页</span>
            <%
                }
            %>
            
            <%
                if (currentPage < totalPages) {
                    String url = "ListDspDataServlet?page=" + totalPages + "&size=" + size;
                    if (currency != null && !currency.isEmpty()) {
                        url += "&currency=" + currency;
                    }
                    if (month != null && !month.isEmpty()) {
                        url += "&month=" + month;
                    }
            %>
                <a href="<%= url %>">末页</a>
            <%
                } else {
            %>
                <span class="disabled">末页</span>
            <%
                }
            %>
            <%
                }
            %>
        </div>
    </div>
    
    <script>
        function showCurrencyList() {
            var currencyList = document.getElementById('currencyList');
            if (currencyList) {
                currencyList.style.display = 'block';
            }
        }
        
        function setCurrency(currency) {
            var currencyInput = document.getElementById('currencyInput');
            var currencyList = document.getElementById('currencyList');
            
            if (currencyInput) {
                currencyInput.value = currency;
            }
            if (currencyList) {
                currencyList.style.display = 'none';
            }
        }
        
        function resetForm() {
            var currencyInput = document.getElementById('currencyInput');
            if (currencyInput) {
                currencyInput.value = "";
            }
            
            var monthInput = document.querySelector('input[name="month"]');
            if (monthInput) {
                monthInput.value = "";
            }
            
            var form = document.getElementById('searchForm');
            if (form) {
                form.submit();
            }
            
            var currencyList = document.getElementById('currencyList');
            if (currencyList) {
                currencyList.style.display = 'none';
            }
        }
        
        document.addEventListener('click', function(event) {
            var currencyList = document.getElementById('currencyList');
            var currencyInput = document.getElementById('currencyInput');
            
            if (currencyList && currencyInput) {
                if (!currencyList.contains(event.target) && event.target !== currencyInput) {
                    currencyList.style.display = 'none';
                }
            }
        });
    </script>
</body>
</html>