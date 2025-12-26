package com.servlet; // 账号系统列表

import com.dao.AccountDAO;
import com.dao.OperationLogZhDAO;
import com.dao.V3DAO;
import com.model.AccountData;
import com.model.OperationLogZh;
import com.model.User;
import com.model.V3;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

@WebServlet("/AccountManagementServlet")
public class AccountManagementServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        // 检查用户权限
        User loginUser = (User) request.getSession().getAttribute("loginUser");
        if (loginUser == null || !"yes".equalsIgnoreCase(loginUser.getIsSeeAccount())) {
            response.sendRedirect("index.jsp");
            return;
        }
        // ✅✅✅ 在此处插入：记录“查看”日志 ✅✅✅
        OperationLogZh logZh = new OperationLogZh();
        logZh.setUserId(loginUser.getId());
        logZh.setUsername(loginUser.getUsername());
        logZh.setOperationType("VIEW");
        logZh.setOperationDesc("查看账号管理列表页面");
        logZh.setIpAddress(request.getRemoteAddr());
        new OperationLogZhDAO().insertLogZh(logZh);
        // ✅✅✅ 日志记录结束 ✅✅✅
        // 获取分页参数
        int page = 1;
        int size = 10; // 默认每页10条
        // 新增：字段选择 + 关键词
        String searchField = request.getParameter("searchField");
        String keyword = request.getParameter("keyword");
        String receiptStatus = request.getParameter("receiptStatus");
        String shopStatus = request.getParameter("shopStatus");

        // 安全：只允许白名单字段（防止非法字段注入）
        Set<String> allowedFields = new HashSet<>();
        allowedFields.add("mains");
        allowedFields.add("acc_name");
        allowedFields.add("ziniao");
        allowedFields.add("type_op");
        allowedFields.add("country");
        allowedFields.add("area");
        allowedFields.add("platform");
        allowedFields.add("depart_name");
        
        if (searchField != null && !allowedFields.contains(searchField)) {
            searchField = null; // 非法字段忽略
        }

        try {
            String pageParam = request.getParameter("page");
            if (pageParam != null && !pageParam.isEmpty()) {
                page = Integer.parseInt(pageParam);
                if (page < 1) page = 1;
            }
            
            String sizeParam = request.getParameter("size");
            if (sizeParam != null && !sizeParam.isEmpty()) {
                size = Integer.parseInt(sizeParam);
                if (size < 10) size = 10;
                if (size > 200) size = 200;
            }
        } catch (NumberFormatException e) {
            // 使用默认值
        }

        // 查询数据（带字段搜索、筛选和分页）
        AccountDAO accountDAO = new AccountDAO();
        List<AccountData> accountDataList = accountDAO.searchPaginatedAccountData(
            searchField, keyword, receiptStatus, shopStatus, page, size
        );
        int totalAccounts = accountDAO.countAccountData(searchField, keyword, receiptStatus, shopStatus);
        int activeAccounts = accountDAO.countActiveAccounts(searchField, keyword, receiptStatus, shopStatus);
        int platformCount = accountDAO.countDistinctPlatforms(searchField, keyword, receiptStatus, shopStatus);
        int countryCount = accountDAO.countDistinctCountries(searchField, keyword, receiptStatus, shopStatus);

        // 获取所有部门（用于下拉）
        V3DAO v3DAO = new V3DAO();
        List<V3> allDepartments = v3DAO.getAllDepartments();
        request.setAttribute("allDepartments", allDepartments);

        // 设置请求属性
        request.setAttribute("accountDataList", accountDataList);
        request.setAttribute("totalAccounts", totalAccounts);
        request.setAttribute("activeAccounts", activeAccounts);
        request.setAttribute("platformCount", platformCount);
        request.setAttribute("countryCount", countryCount);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalPages", (int) Math.ceil((double) totalAccounts / size));
     // 获取所有模式（用于模式下拉框）
        List<Map<String, Object>> allModes = accountDAO.getAllModes();
        request.setAttribute("allModes", allModes);

        // 转发到JSP
        request.getRequestDispatcher("account_management.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
        System.out.println("========== 我是真正的账号管理系统 ==========");
    }
}
