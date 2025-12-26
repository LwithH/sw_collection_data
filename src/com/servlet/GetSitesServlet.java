package com.servlet;//获取国家/站点）

import com.dao.AccountDAO;
import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/GetSitesServlet")
public class GetSitesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json; charset=UTF-8");
        AccountDAO dao = new AccountDAO();
        List<Map<String, Object>> sites = dao.getAllSites();
        new Gson().toJson(sites, response.getWriter());
    }
}
