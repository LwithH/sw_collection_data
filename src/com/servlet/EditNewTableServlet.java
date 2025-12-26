package com.servlet;
//福来表的修改界面
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dao.CollectionDAO;
import com.model.CollectionData;
import com.model.User;

@WebServlet("/EditNewTableServlet")
public class EditNewTableServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            CollectionDAO dao = new CollectionDAO();
            CollectionData data = dao.getDataByIdNew(id); // 你需要实现这个方法（见下方）

            if (data == null) {
                response.sendRedirect("ListNewTableServlet?message=" + java.net.URLEncoder.encode("数据不存在", "UTF-8"));
                return;
            }

            // 获取当前登录用户
            HttpSession session = request.getSession();
            User currentUser = (User) session.getAttribute("loginUser");
            if (currentUser == null) {
                response.sendRedirect("login.jsp?message=" + java.net.URLEncoder.encode("请先登录", "UTF-8"));
                return;
            }

            // 检查权限（可选，你已做前端控制，但后端必须校验！）
            if (data.getCreateUserId() != 1 && currentUser.getId() != data.getCreateUserId()) {
                response.sendRedirect("ListNewTableServlet?message=" + java.net.URLEncoder.encode("无权限修改此数据", "UTF-8"));
                return;
            }

            request.setAttribute("data", data);
            request.getRequestDispatcher("/editNewTable.jsp").forward(request, response);
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect("ListNewTableServlet?message=" + java.net.URLEncoder.encode("系统错误", "UTF-8"));
        }
    }
}
