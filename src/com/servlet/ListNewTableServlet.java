package com.servlet;
//福来
import java.io.IOException;
import java.util.List; 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dao.CollectionDAO;
import com.dao.LogDAO;
import com.model.CollectionData;
import com.model.OperationLog;
import com.model.User;
import com.model.PermissionConstants;

@WebServlet("/ListNewTableServlet")
public class ListNewTableServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. 获取登录用户和IP
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            response.sendRedirect("login.jsp");
            return;
        }

        // 👇 权限校验：必须拥有 NEW_ONLY 或 BOTH 权限
      /*  String permission = loginUser.getPermission();
        if (!PermissionConstants.NEW_ONLY.equals(permission) && !PermissionConstants.BOTH.equals(permission)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问新数据表");
            return;
        }*/

        String username = loginUser.getUsername();
        String ip = request.getRemoteAddr();

        // 2. 获取页码参数
        String pageStr = request.getParameter("page");
        int page = 1;
        if (pageStr != null && !pageStr.isEmpty()) {
            try {
                page = Integer.parseInt(pageStr);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                page = 1;
            }
        }

        // 3. 获取每页条数
        int pageSize = 20;
        try {
            String pageSizeStr = request.getParameter("pageSize");
            if (pageSizeStr != null && !pageSizeStr.trim().isEmpty()) {
                pageSize = Integer.parseInt(pageSizeStr);
                if (pageSize <= 0) pageSize = 20;
                if (pageSize > 500) pageSize = 500;
            }
        } catch (NumberFormatException e) {
            pageSize = 20;
        }
        
        // 4. 获取搜索参数
        String searchKeyword = request.getParameter("searchKeyword");
        if (searchKeyword != null) {
            searchKeyword = searchKeyword.trim();
        }
        
        // 5. 获取筛选参数
        boolean needEdit = "true".equals(request.getParameter("needEdit"));

        // 6. 查询新表数据（支持搜索和筛选）
        CollectionDAO collectionDAO = new CollectionDAO();
        List<CollectionData> dataList = collectionDAO.searchNewDataWithFilter(searchKeyword, needEdit, page, pageSize);
        int totalCount = collectionDAO.getTotalNewCountWithFilter(searchKeyword, needEdit);

        // 计算总页数
        int totalPages = (totalCount + pageSize - 1) / pageSize;
        if (totalPages < 1) totalPages = 1;
        if (page > totalPages) page = totalPages;

        // 7. 设置请求属性
        request.setAttribute("dataList", dataList);
        request.setAttribute("currentPage", page);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("totalCount", totalCount);
        request.setAttribute("pageSize", pageSize);
        request.setAttribute("searchKeyword", searchKeyword);
        request.setAttribute("needEdit", needEdit);

        // 8. 跳转到新表列表页面
        request.getRequestDispatcher("list_new.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doGet(request, response);
    }
}
