package com.servlet;
//品牌广告列表
import com.model.BrandAd;
import com.model.User;
import com.dao.BrandAdDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet("/ListBrandAdServlet")
public class ListBrandAdServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("loginUser");

        if (loginUser == null || !"yes".equalsIgnoreCase(loginUser.getIsSeeAd())) {
            request.setAttribute("message", "您没有权限访问品牌广告数据！");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        // ✅ 获取当前用户的部门权限
        String userDepart = loginUser.getUserDepart(); // 如 "00" 或 "1,2,3,4"

        int page = getIntParameter(request, "page", 1);
        int size = getIntParameter(request, "size", 10);
        if (size != 10 && size != 20 && size != 50 && size != 100) {
            size = 10;
        }

        String campaignName = request.getParameter("campaignName");
        String sku = request.getParameter("sku");
        String uacs = request.getParameter("uacs");

        BrandAdDAO dao = new BrandAdDAO();
        List<BrandAd> brandAds;
        int total;

        boolean hasSearch = (campaignName != null && !campaignName.trim().isEmpty()) ||
                            (sku != null && !sku.trim().isEmpty()) ||
                            (uacs != null && !uacs.trim().isEmpty());

        if (hasSearch) {
            // ✅ 传递 userDepart
            brandAds = dao.searchWithPagination(userDepart, campaignName, sku, uacs, page, size);
            total = dao.countSearch(userDepart, campaignName, sku, uacs);
        } else {
            // ✅ 传递 userDepart
            brandAds = dao.findWhereSkuIsEmptyWithPagination(userDepart, page, size);
            total = dao.countWhereSkuIsEmpty(userDepart);
        }

        request.setAttribute("brandAds", brandAds);
        request.setAttribute("total", total);
        request.getRequestDispatcher("brand_ad_list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        doGet(req, resp);
    }

    private int getIntParameter(HttpServletRequest request, String paramName, int defaultValue) {
        String value = request.getParameter(paramName);
        if (value != null && !value.isEmpty()) {
            try {
                int num = Integer.parseInt(value);
                return num > 0 ? num : defaultValue;
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}
