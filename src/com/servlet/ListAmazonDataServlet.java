// com.servlet.ListAmazonDataServlet.java
// 亚马逊权限校验 对temu和福来表
package com.servlet;

import com.dao.AmazonDataDAO;
import com.model.AmazonData;
import com.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/ListAmazonDataServlet")
public class ListAmazonDataServlet extends HttpServlet {

    private static final int MAX_OFFSET_PAGES = 100;
    
    // 部门中文名称到数字代码的映射
    private static final Map<String, String> DEPARTMENT_MAP = new HashMap<>();
    
    static {
        // 初始化部门映射
        DEPARTMENT_MAP.put("销售一部", "1");
        DEPARTMENT_MAP.put("销售二部", "2");
        DEPARTMENT_MAP.put("乐器项目部", "3");
        DEPARTMENT_MAP.put("大件项目部", "4");
        DEPARTMENT_MAP.put("销售三部", "5");
        DEPARTMENT_MAP.put("天津项目部", "6");
        DEPARTMENT_MAP.put("工业项目部", "7");
        DEPARTMENT_MAP.put("Ali", "97");
        DEPARTMENT_MAP.put("深圳公司", "98");
        DEPARTMENT_MAP.put("停用", "99");
        DEPARTMENT_MAP.put("未分配", "0");
        DEPARTMENT_MAP.put("-", "0");
        DEPARTMENT_MAP.put("空", "0");
        DEPARTMENT_MAP.put("无", "0");
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("loginUser");

        if (user == null || !"yes".equalsIgnoreCase(user.getIsSeeAmazon())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问亚马逊数据");
            return;
        }

        String searchField = request.getParameter("searchField");
        String keyword = request.getParameter("keyword");
        String originalKeyword = keyword; // 保存原始关键词用于回显
        
        // 特殊处理：如果是部门字段搜索，处理中文部门名称
        boolean isDepartmentSearch = false;
        String departmentSearchKeyword = null;
        
        // 获取第二个搜索条件
        String searchField2 = request.getParameter("searchField2");
        String keyword2 = request.getParameter("keyword2");
        String originalKeyword2 = keyword2; // 保存原始关键词用于回显

        // 添加到请求属性（用于JSP回显）
        request.setAttribute("searchField2", searchField2);
        request.setAttribute("keyword2", originalKeyword2);

        if ("sales_depart".equals(searchField) && keyword != null && !keyword.trim().isEmpty()) {
            isDepartmentSearch = true;
            String chineseKeyword = keyword.trim();
            
            // 检查是否是完整的部门名称
            String code = DEPARTMENT_MAP.get(chineseKeyword);
            if (code != null) {
                // 完整部门名称，转换为单个数字代码
                departmentSearchKeyword = code;
            } else {
                // 尝试模糊匹配：找出所有包含该关键词的部门名称
                List<String> matchedCodes = new ArrayList<>();
                for (Map.Entry<String, String> entry : DEPARTMENT_MAP.entrySet()) {
                    if (entry.getKey().contains(chineseKeyword)) {
                        matchedCodes.add(entry.getValue());
                    }
                }
                
                if (!matchedCodes.isEmpty()) {
                    // 多个部门代码，构建IN查询条件，如：1,2,5
                    departmentSearchKeyword = String.join(",", matchedCodes);
                } else {
                    // 检查是否已经是数字
                    try {
                        Integer.parseInt(chineseKeyword);
                        // 是数字，直接使用
                        departmentSearchKeyword = chineseKeyword;
                    } catch (NumberFormatException e) {
                        // 不是数字，尝试部分匹配
                        // 如果输入"销售"，匹配所有以销售开头的部门
                        List<String> partialMatchedCodes = new ArrayList<>();
                        for (Map.Entry<String, String> entry : DEPARTMENT_MAP.entrySet()) {
                            if (entry.getKey().startsWith(chineseKeyword)) {
                                partialMatchedCodes.add(entry.getValue());
                            }
                        }
                        
                        if (!partialMatchedCodes.isEmpty()) {
                            departmentSearchKeyword = String.join(",", partialMatchedCodes);
                        } else {
                            // 最后尝试单个字匹配，如"销"
                            if (chineseKeyword.length() == 1) {
                                List<String> singleCharMatchedCodes = new ArrayList<>();
                                for (Map.Entry<String, String> entry : DEPARTMENT_MAP.entrySet()) {
                                    if (entry.getKey().contains(chineseKeyword)) {
                                        singleCharMatchedCodes.add(entry.getValue());
                                    }
                                }
                                
                                if (!singleCharMatchedCodes.isEmpty()) {
                                    departmentSearchKeyword = String.join(",", singleCharMatchedCodes);
                                } else {
                                    // 没有匹配到任何部门，设置为"0"表示未分配
                                    departmentSearchKeyword = "0";
                                }
                            } else {
                                // 没有匹配到任何部门，设置为"0"表示未分配
                                departmentSearchKeyword = "0";
                            }
                        }
                    }
                }
            }
        }

        boolean filterEmptySeller = "on".equals(request.getParameter("emptySeller"));

        Integer year = null;
        String yearParam = request.getParameter("year");
        if (yearParam != null && !yearParam.isEmpty()) {
            try {
                year = Integer.parseInt(yearParam);
            } catch (NumberFormatException ignored) {}
        }

        int page = getIntParameter(request, "page", 1);
        int size = getIntParameter(request, "size", 10);
        if (size != 10 && size != 20 && size != 50 && size != 100 && size != 200) {
            size = 10;
        }

        AmazonDataDAO dao = new AmazonDataDAO();
        
        // 如果是部门搜索，使用转换后的关键词
        String searchKeyword = isDepartmentSearch ? departmentSearchKeyword : keyword;
        
        // === 修复点1: 添加第二个搜索条件到getTotalCount调用 ===
        int total = dao.getTotalCount(searchField, searchKeyword, searchField2, keyword2, filterEmptySeller, year);
        // === 修复点2: 添加第二个搜索条件到getEmptySellerCount调用 ===
        int emptySellerCount = dao.getEmptySellerCount(searchField, searchKeyword, searchField2, keyword2, year);
        int filledSellerCount = total - emptySellerCount;
        int totalPages = total == 0 ? 1 : (int) Math.ceil((double) total / size);

        List<AmazonData> dataList;
        if (page <= MAX_OFFSET_PAGES) {
            int offset = (page - 1) * size;
            // === 修复点3: 添加第二个搜索条件到getAmazonDataByPage调用 ===
            dataList = dao.getAmazonDataByPage(
                searchField, searchKeyword, 
                searchField2, keyword2,
                filterEmptySeller, year, offset, size);
        } else {
            int previousPageLastIndex = page * size;
            int cursorOffset = previousPageLastIndex - size;

            if (cursorOffset >= total) {
                dataList = new ArrayList<>();
            } else {
                // === 修复点4: 添加第二个搜索条件到getIdAtOffset调用 ===
                int lastId = dao.getIdAtOffset(searchField, searchKeyword, searchField2, keyword2, filterEmptySeller, year, cursorOffset);
                if (lastId == -1) {
                    dataList = new ArrayList<>();
                } else {
                    // === 修复点5: 添加第二个搜索条件到getAmazonDataAfterId调用 ===
                    dataList = dao.getAmazonDataAfterId(searchField, searchKeyword, searchField2, keyword2, filterEmptySeller, year, lastId, size);
                }
            }
        }

        // === 构建 commonParams（用于分页链接）===
        StringBuilder commonParams = new StringBuilder();
        try {
            if (searchField != null && !searchField.isEmpty()) {
                commonParams.append("&searchField=").append(URLEncoder.encode(searchField, "UTF-8"));
            }
            // 使用原始关键词（用户输入的中文）构建URL参数，以便回显
            if (originalKeyword != null && !originalKeyword.isEmpty()) {
                commonParams.append("&keyword=").append(URLEncoder.encode(originalKeyword, "UTF-8"));
            }
            
            // === 修复点6: 添加第二个搜索条件到commonParams ===
            if (searchField2 != null && !searchField2.isEmpty()) {
                commonParams.append("&searchField2=").append(URLEncoder.encode(searchField2, "UTF-8"));
            }
            if (keyword2 != null && !keyword2.isEmpty()) {
                commonParams.append("&keyword2=").append(URLEncoder.encode(keyword2, "UTF-8"));
            }
            
            if (filterEmptySeller) {
                commonParams.append("&emptySeller=on");
            }
            if (year != null) {
                commonParams.append("&year=").append(year);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        request.setAttribute("dataList", dataList);
        request.setAttribute("currentPage", page);
        request.setAttribute("pageSize", size);
        request.setAttribute("totalPages", totalPages);
        request.setAttribute("total", total);
        request.setAttribute("totalRecords", total);
        request.setAttribute("emptySellerCount", emptySellerCount);
        request.setAttribute("filledSellerCount", filledSellerCount);
        request.setAttribute("searchField", searchField);
        // 使用原始关键词回显到搜索框
        request.setAttribute("keyword", originalKeyword);
        request.setAttribute("emptySellerChecked", filterEmptySeller ? "checked" : "");
        request.setAttribute("selectedYear", year);
        request.setAttribute("commonParams", commonParams.toString());

        request.getRequestDispatcher("/listAmazonData.jsp").forward(request, response);
    }

    private int getIntParameter(HttpServletRequest req, String name, int defaultValue) {
        try {
            return Integer.parseInt(req.getParameter(name));
        } catch (NumberFormatException | NullPointerException e) {
            return defaultValue;
        }
    }
}
