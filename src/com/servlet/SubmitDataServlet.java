package com.servlet;//首页提交逻辑

import java.io.IOException;
import java.time.LocalDateTime;

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
import com.model.PermissionConstants;
import com.model.User;

@WebServlet("/SubmitDataServlet")
public class SubmitDataServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        // 1. 获取登录用户信息
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            request.setAttribute("message", "请先登录！");
            request.getRequestDispatcher("login.jsp").forward(request, response);
            return;
        }
        String username = loginUser.getUsername();
        String ip = request.getRemoteAddr();
      /*  String permission = loginUser.getPermission();
        if (!PermissionConstants.OLD_ONLY.equals(permission) && !PermissionConstants.BOTH.equals(permission)) {
            request.setAttribute("message", "无权提交数据到该表！");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }*/
        // 获取用户ID
        int userId = loginUser.getId(); 

        // 2. 获取表单数据
        String sku = request.getParameter("sku");
        String seller = request.getParameter("seller");

        // 3. 基础校验
        if (sku == null || sku.trim().isEmpty() || seller == null || seller.trim().isEmpty()) {
            request.setAttribute("message", "错误：SKU和Seller为必填字段！");
            request.setAttribute("filledSku", sku);
            request.setAttribute("filledSeller", seller);
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        // 4. 检查SKU是否已存在
        CollectionDAO collectionDAO = new CollectionDAO();
        if (collectionDAO.isSkuExists(sku.trim())) {
            request.setAttribute("message", "⚠️ 该sku已经存在系统，请点击导航栏temu数据表，进行变更销售员操作或联系系统管理员");
            request.setAttribute("filledSku", sku);
            request.setAttribute("filledSeller", seller);
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        // 5. Seller合法性校验并获取user_id_ding
        SellerNameDAO sellerDAO = new SellerNameDAO();
        String trimmedSeller = seller.trim();
        String userIdDing = sellerDAO.validateSellerAndGetUserIdDing(trimmedSeller);
        
        if (userIdDing == null) {
            request.setAttribute("message", "⚠️ 错误：Seller【" + trimmedSeller + "】不存在或已失效，请检查输入或联系系统管理员！");
            request.setAttribute("filledSku", sku);
            request.setAttribute("filledSeller", seller);
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        // 6. 自动设置ISC1
        String isc1 = "首位";

        // 7. 提交数据到数据库
        CollectionData data = new CollectionData(sku.trim(), trimmedSeller, isc1, userId);
        data.setUserIdDing(userIdDing); // 设置userIdDing
        boolean success = collectionDAO.insertData(data);

     // 8. 记录操作日志（新增user_id_ding记录）
        LogDAO logDAO = new LogDAO();
        String logContent = String.format(
            "用户[%s]提交数据 - SKU:%s, Seller:%s, ISC1:%s, user_id_ding:%s（自动设置）",
            username,sku.trim(), trimmedSeller, isc1, userIdDing
        );
        
        OperationLog log = new OperationLog(
            success ? "数据提交成功" : "数据提交失败",
            logContent,
            ip,
            username
        );
        log.setCreateTime(LocalDateTime.now());
        logDAO.logOperation(log);

        // 9. 跳转结果页
        if (success) {
            request.setAttribute("message", "数据提交成功！ISC1已自动设置为「首位」");
        } else {
            request.setAttribute("message", "数据提交失败，请重试！");
        }
        request.getRequestDispatcher("result.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }
}
