package com.servlet;//temu修改与日志逻辑


import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.List;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import com.dao.CollectionDAO;
import com.dao.LogDAO;
import com.dao.SellerNameDAO;
import com.model.CollectionData;
import com.model.OperationLog;
import com.model.User;
import javax.servlet.http.HttpSession;
import com.model.User;
import com.model.PermissionConstants;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/SearchModifyServlet")
public class SearchModifyServlet extends HttpServlet {
private static final long serialVersionUID = 1L;


@Override
protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");
    HttpSession session = request.getSession();
    User loginUser = (User) session.getAttribute("loginUser");
    if (loginUser == null) {
        response.sendRedirect("login.jsp");
        return;
    }

   /* String permission = loginUser.getPermission();
    if (!PermissionConstants.OLD_ONLY.equals(permission) && !PermissionConstants.BOTH.equals(permission)) {
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权操作原数据表");
        return;
    }*/
    // 获取所有请求参数
    String action = request.getParameter("action");
    String keyword = request.getParameter("keyword");
    String pageStr = request.getParameter("page");
    String pageSizeStr = request.getParameter("pageSize");
    String filter = request.getParameter("filter");

    // 设置默认分页值
    int page = 1;
    int pageSize = 10;

    // 处理 page 参数
    try {
        if (pageStr != null && !pageStr.isEmpty()) {
            page = Integer.parseInt(pageStr);
            if (page < 1) page = 1;
        }
        if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
            pageSize = Integer.parseInt(pageSizeStr);
            if (pageSize <= 0) pageSize = 10;
            if (pageSize > 500) pageSize = 500;
        }
    } catch (NumberFormatException e) {
        page = 1;
        pageSize = 10;
    }

    CollectionDAO collectionDAO = new CollectionDAO();
    List<CollectionData> dataList;
    int totalCount;
    int totalPages;

    // 处理“查看日志”动作
    if ("viewLog".equals(action)) {
        try {
            int dataId = Integer.parseInt(request.getParameter("id"));
            LogDAO logDAO = new LogDAO();
            List<OperationLog> logs = logDAO.getLogsByDataId(dataId);

            request.setAttribute("logs", logs);
            request.setAttribute("dataId", dataId);
            request.setAttribute("keyword", keyword);
            request.setAttribute("page", page);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("filter", filter);
            request.getRequestDispatcher("view_logs.jsp").forward(request, response);
            return;
        } catch (NumberFormatException e) {
            request.setAttribute("message", "参数错误！");
            redirectToSearchPage(response, keyword, page, pageSize, filter);
            return;
        }
    }

    // 处理“编辑”动作
    if ("edit".equals(action)) {
        handleEdit(request, response, page, pageSize, keyword, filter);
        return;
    }

    // 处理“更新”动作
    if ("update".equals(action)) {
        handleUpdate(request, response);
        return;
    }

    // 核心：搜索数据并渲染列表页
    CollectionDAO dao = new CollectionDAO();
    if ("emptySeller".equals(filter)) {
        dataList = dao.getEmptySellerData(keyword, page, pageSize);
        totalCount = dao.getTotalEmptySellerCount(keyword);
        totalPages = (totalCount + pageSize - 1) / pageSize;
    } else if (keyword != null && !keyword.trim().isEmpty()) {
        dataList = dao.searchData(keyword.trim(), page, pageSize);
        totalCount = dao.getTotalCountWithKeyword(keyword.trim());
        totalPages = (totalCount + pageSize - 1) / pageSize;
    } else {
        dataList = dao.getAllData(page, pageSize);
        totalCount = dao.getTotalCount();
        totalPages = (totalCount + pageSize - 1) / pageSize;
    }

    // 防止页码越界
    if (totalPages < 1) totalPages = 1;
    if (page > totalPages) page = totalPages;

    // 传递数据到页面
    request.setAttribute("dataList", dataList);
    request.setAttribute("keyword", keyword);
    request.setAttribute("currentPage", page);
    request.setAttribute("totalPages", totalPages);
    request.setAttribute("totalCount", totalCount);
    request.setAttribute("pageSize", pageSize);
    request.setAttribute("filter", filter);

    // 转发到列表页
    request.getRequestDispatcher("searchModify.jsp").forward(request, response);
}

/**
 * 处理编辑请求：URL拼接分页参数，确保editForm.jsp能获取到
 */
private void handleEdit(HttpServletRequest request, HttpServletResponse response, 
                       int page, int pageSize, String keyword, String filter) throws ServletException, IOException {
    try {
        int id = Integer.parseInt(request.getParameter("id"));
        CollectionDAO collectionDAO = new CollectionDAO();
        CollectionData data = collectionDAO.getDataById(id);

        if (data != null) {
            request.setAttribute("data", data);
            String encodedKeyword = keyword != null ? URLEncoder.encode(keyword, "UTF-8") : "";
            String editUrl = String.format("editForm.jsp?page=%d&pageSize=%d&keyword=%s&filter=%s",
                                          page, pageSize, encodedKeyword, filter);
            request.getRequestDispatcher(editUrl).forward(request, response);
        } else {
            request.setAttribute("message", "未找到该数据！");
            redirectToSearchPage(response, keyword, page, pageSize, filter);
        }
    } catch (NumberFormatException e) {
        request.setAttribute("message", "参数错误！");
        redirectToSearchPage(response, keyword, page, pageSize, filter);
    }
}

/** 
 * 处理更新请求：支持ISC1为空 + ISC1自动更新 + seller合法性校验（基于sellername表）
 */
private void handleUpdate(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    request.setCharacterEncoding("UTF-8");

    try {
        int id = Integer.parseInt(request.getParameter("id"));
        String sku = request.getParameter("sku");
        String newSeller = request.getParameter("seller"); // 新的人名（待校验）
        String inputIsc1 = request.getParameter("isc1");   // 用户输入的ISC1（可为空）
        String keyword = request.getParameter("keyword");
        String pageStr = request.getParameter("page");
        String pageSizeStr = request.getParameter("pageSize");
        String filter = request.getParameter("filter");

        int page = 1;
        int pageSize = 10;
        if (pageStr != null && !pageStr.isEmpty()) {
            page = Integer.parseInt(pageStr);
            if (page < 1) page = 1;
        }
        if (pageSizeStr != null && !pageSizeStr.isEmpty()) {
            pageSize = Integer.parseInt(pageSizeStr);
            if (pageSize <= 0) pageSize = 10;
            if (pageSize > 500) pageSize = 500;
        }

        // -------------------------- 1. 基础表单校验（SKU和Seller必填）--------------------------
        if (sku == null || sku.trim().isEmpty() ||
            newSeller == null || newSeller.trim().isEmpty()) {
            request.setAttribute("message", "SKU和Seller为必填字段！");
            CollectionData data = new CollectionData(sku, newSeller, inputIsc1);
            data.setId(id);
            request.setAttribute("data", data);
            request.setAttribute("page", page);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("keyword", keyword);
            request.setAttribute("filter", filter);
            request.getRequestDispatcher("editForm.jsp").forward(request, response);
            return;
        }

        // -------------------------- 2. 新增：seller合法性校验并获取user_id_ding --------------------------
        SellerNameDAO sellerDAO = new SellerNameDAO();
        String userIdDing = sellerDAO.validateSellerAndGetUserIdDing(newSeller);
        if (userIdDing == null) {
            // 校验失败：设置错误消息+回显数据，终止更新
            String errorMsg = String.format("⚠️ 填入的Seller【%s】不存在或已失效，请重新输入！", newSeller.trim());
            request.setAttribute("message", errorMsg);
            CollectionData data = new CollectionData(sku, newSeller, inputIsc1);
            data.setId(id);
            request.setAttribute("data", data); // 回显已填数据，避免用户重复输入
            request.setAttribute("page", page);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("keyword", keyword);
            request.setAttribute("filter", filter);
            request.getRequestDispatcher("editForm.jsp").forward(request, response);
            return;
        }

        // -------------------------- 3. 原有逻辑：获取旧数据+自动更新ISC1---------------------------
        CollectionDAO collectionDAO = new CollectionDAO();
        CollectionData oldData = collectionDAO.getDataById(id);
        if (oldData == null) {
            request.setAttribute("message", "未找到该数据！");
            redirectToSearchPage(response, keyword, page, pageSize, filter);
            return;
        }
        String oldSeller = oldData.getSeller();
        String oldIsc1 = oldData.getIsc1();

        // 核心逻辑：根据seller变更自动计算新ISC1
        String newIsc1;
        boolean isSellerChanged = !newSeller.trim().equals(
            oldSeller != null ? oldSeller.trim() : ""
        );

        if (isSellerChanged) {
            if (oldIsc1 == null || oldIsc1.trim().isEmpty()) {
                newIsc1 = "首位";
            } else {
                newIsc1 = "接手";
            }
        } else {
            newIsc1 = inputIsc1 != null ? inputIsc1.trim() : null;
        }

        // -------------------------- 4. 执行更新+记录日志--------------
        // 获取当前登录用户ID
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            request.setAttribute("message", "请先登录！");
            redirectToSearchPage(response, keyword, page, pageSize, filter);
            return;
        }
        int updateUserId = loginUser.getId(); // 获取用户ID
        
        // 创建新数据对象并设置updateUserId和userIdDing
        CollectionData newData = new CollectionData(
            sku.trim(), 
            newSeller.trim(), 
            newIsc1
        );
        newData.setId(id);
        newData.setUpdateUserId(updateUserId);
        newData.setUserIdDing(userIdDing); // 设置userIdDing
        
        boolean success = collectionDAO.updateData(newData);

        // 记录操作日志（处理空值显示）
        String username = loginUser.getUsername();
        String ip = request.getRemoteAddr();
        LogDAO logDAO = new LogDAO();
        
        String logOldIsc1 = (oldIsc1 == null || oldIsc1.trim().isEmpty()) ? "空" : oldIsc1;
        String logNewIsc1 = (newIsc1 == null || newIsc1.trim().isEmpty()) ? "空" : newIsc1;
        String logContent = String.format(
            "用户[%s]从 ID:%d, SKU:%s, Seller:%s, ISC1:%s 修改为 ID:%d, SKU:%s, Seller:%s, ISC1:%s",
            username, id, oldData.getSku(), oldSeller, logOldIsc1,
            id, newData.getSku(), newData.getSeller(), logNewIsc1
        );
        
        OperationLog log = new OperationLog(
            success ? "数据修改成功" : "数据修改失败", 
            logContent, 
            ip, 
            username
        );
        log.setCreateTime(LocalDateTime.now());
        logDAO.logOperation(log);

        // 重定向到列表页
        redirectToSearchPage(response, keyword, page, pageSize, filter);
    } catch (Exception e) {
        request.setAttribute("message", "参数错误！");
        String keyword = request.getParameter("keyword");
        String pageStr = request.getParameter("page");
        String pageSizeStr = request.getParameter("pageSize");
        String filter = request.getParameter("filter");
        int page = pageStr != null ? Integer.parseInt(pageStr) : 1;
        int pageSize = pageSizeStr != null ? Integer.parseInt(pageSizeStr) : 10;
        redirectToSearchPage(response, keyword, page, pageSize, filter);
    }
}


/**
 * 工具方法：重定向到搜索列表页（统一处理参数编码）
 */
private void redirectToSearchPage(HttpServletResponse response, String keyword, int page, int pageSize, String filter)
        throws IOException {
    String encodedKeyword = keyword != null ? URLEncoder.encode(keyword, "UTF-8") : "";
    String redirectUrl = String.format("SearchModifyServlet?keyword=%s&page=%d&pageSize=%d&filter=%s",
                                    encodedKeyword, page, pageSize, filter);
    response.sendRedirect(redirectUrl);
}

@Override
protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
    doGet(request, response);
}

}