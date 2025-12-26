// com.servlet.GetSystemFieldsServlet 账号系统的修改界面
package com.servlet;

import com.dao.AccountDAO;
import com.google.gson.Gson; // 需要引入 Gson
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/GetSystemFieldsServlet")
public class GetSystemFieldsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        try {
            int accountId = Integer.parseInt(request.getParameter("accountId"));
            AccountDAO dao = new AccountDAO();
            Map<String, String> fields = dao.getSystemFieldsById(accountId);
            
            if (fields.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            
            // 确保 s1~s20 都存在（即使为 null）
            for (int i = 1; i <= 20; i++) {
                fields.putIfAbsent("s" + i, "");
            }
            
            new Gson().toJson(fields, response.getWriter());
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}
