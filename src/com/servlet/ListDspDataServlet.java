package com.servlet;

import com.dao.DspDAO;
import com.model.Dsp;
import com.util.PageUtil;
import com.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@WebServlet("/ListDspDataServlet")
public class ListDspDataServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 检查权限
        User loginUser = (User) request.getSession().getAttribute("loginUser");
        if (loginUser == null || !"yes".equalsIgnoreCase(loginUser.getIsSeeDsp())) {
            response.sendRedirect("index.jsp");
            return;
        }
        
        // 获取分页参数
        int page = 1;
        int size = 28; // 默认每页28条
        String pageParam = request.getParameter("page");
        String sizeParam = request.getParameter("size");
        if (pageParam != null && !pageParam.isEmpty()) {
            page = Integer.parseInt(pageParam);
        }
        if (sizeParam != null && !sizeParam.isEmpty()) {
            size = Integer.parseInt(sizeParam);
        }

        // 获取搜索参数
        String currency = request.getParameter("currency");
        String month = request.getParameter("month");
        
        // 将月份数转换为日期范围
        Date startDate = null;
        Date endDate = null;
        // 修复：检查month是否为"null"字符串
        if (month != null && !"null".equals(month) && !month.isEmpty()) {
            try {
                // 将"YYYY-MM"格式的月份转换为该月的第一天和最后一天
                SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");
                Date monthDate = monthFormat.parse(month);
                
                // 获取该月的第一天
                SimpleDateFormat firstDayFormat = new SimpleDateFormat("yyyy-MM-01");
                String firstDay = firstDayFormat.format(monthDate);
                startDate = new SimpleDateFormat("yyyy-MM-dd").parse(firstDay);
                
                // 获取该月的最后一天
                Calendar cal = Calendar.getInstance();
                cal.setTime(monthDate);
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                endDate = cal.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                // 可以添加错误处理逻辑，例如显示错误消息
            }
        }

        // 查询数据
        DspDAO dspDAO = new DspDAO();
        List<Dsp> dspList = dspDAO.searchDspData(currency, startDate, endDate, page, size);
        int total = dspDAO.countDspData(currency, startDate, endDate);

        // 分页信息
        PageUtil pageUtil = new PageUtil(page, size, total);

        // 设置请求属性
        request.setAttribute("dspList", dspList);
        request.setAttribute("pageUtil", pageUtil);
        request.setAttribute("currency", currency);
        request.setAttribute("month", month);
        request.setAttribute("size", size); // 添加size属性，供JSP使用

        // 转发到JSP
        request.getRequestDispatcher("ListDspData.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
