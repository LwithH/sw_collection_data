// com.servlet.UpdateSalesDepartServlet    //账号系统修改部门
package com.servlet;

import com.dao.AccountDAO;
import com.dao.OperationLogZhDAO;
import com.dao.V3DAO;
import com.model.OperationLogZh;
import com.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/UpdateSalesDepartServlet")
public class UpdateSalesDepartServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            int accountId = Integer.parseInt(request.getParameter("accountId"));
            int newSalesDepart = Integer.parseInt(request.getParameter("newSalesDepart"));
            String currentPage = request.getParameter("currentPage");
            String pageSize = request.getParameter("pageSize");
            V3DAO v3DAO = new V3DAO();
            String newDepartName = v3DAO.getDepartNameById(newSalesDepart); // ← 这行定义了 newDepartName
            AccountDAO dao = new AccountDAO();
            dao.updateSalesDepart(accountId, newSalesDepart);
            // ✅✅✅ 记录“修改”日志 ✅✅✅
            User loginUser = (User) request.getSession().getAttribute("loginUser");
            if (loginUser != null) {
                OperationLogZh logZh = new OperationLogZh();
                logZh.setUserId(loginUser.getId());
                logZh.setUsername(loginUser.getUsername());
                logZh.setOperationType("UPDATE");
                logZh.setOperationDesc("修改店铺ID=" + accountId + " 的销售部门为: " + newDepartName);
                logZh.setIpAddress(request.getRemoteAddr());
                new OperationLogZhDAO().insertLogZh(logZh);
            }
            // ✅✅✅ 日志结束 ✅✅✅
            // 重定向回账号管理页，保留分页参数
            String redirectUrl = "AccountManagementServlet?page=" + (currentPage != null ? currentPage : "1");
            if (pageSize != null) {
                redirectUrl += "&size=" + pageSize;
            }
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("AccountManagementServlet");
        }
    }
}
