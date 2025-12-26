package com.servlet; //福来修改代码

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.dao.CollectionDAO;
import com.model.CollectionData;
import com.model.User;

@WebServlet("/UpdateNewTableServlet")
public class UpdateNewTableServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        // 1️⃣ 获取所有筛选和分页参数
        String page = request.getParameter("page");
        String pageSize = request.getParameter("pageSize");
        String searchKeyword = request.getParameter("searchKeyword");
        String needEdit = request.getParameter("needEdit");
        
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String warehouseSku = request.getParameter("warehouseSku");
            
            // 输入清理
            if (warehouseSku != null) {
                warehouseSku = warehouseSku.trim().toUpperCase();
            }

            HttpSession session = request.getSession();
            User currentUser = (User) session.getAttribute("loginUser");
            if (currentUser == null) {
                response.sendRedirect("login.jsp?message=" + 
                    java.net.URLEncoder.encode("请先登录", "UTF-8"));
                return;
            }

            CollectionDAO dao = new CollectionDAO();
            CollectionData data = dao.getDataByIdNew(id);
            if (data == null) {
                redirectToEditPage(request, response, String.valueOf(id), "数据不存在", page, pageSize, searchKeyword, needEdit);
                return;
            }

            // 验证仓库SKU是否存在
            if (!dao.existsWarehouseSku(warehouseSku)) {
                // SKU不存在 - 显示错误提示
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                
                // 构建编辑页URL（带错误信息和所有参数）
                String editUrl = buildListUrl(page, pageSize, searchKeyword, needEdit);
                editUrl += "&id=" + id + "&message=" + java.net.URLEncoder.encode("仓库SKU不存在，请检查输入", "UTF-8");
                
                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<title>验证失败</title>");
                out.println("<style>");
                out.println("body { font-family: 'Microsoft YaHei', Arial, sans-serif; text-align: center; padding: 50px 20px; background: #f5f5f5; }");
                out.println(".container { max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }");
                out.println("h2 { color: #f56c6c; margin-bottom: 20px; }");
                out.println(".message { font-size: 18px; margin: 25px 0; }");
                out.println(".countdown { font-weight: bold; color: #f56c6c; }");
                out.println(".btn { display: inline-block; margin-top: 20px; padding: 10px 24px; background: #606266; color: white; text-decoration: none; border-radius: 4px; font-size: 16px; }");
                out.println(".btn-return { background: #f56c6c; }");
                out.println(".btn-return:hover { background: #e65151; }");
                out.println(".btn-edit { background: #67c23a; }");
                out.println(".btn-edit:hover { background: #5daf34; }");
                out.println(".btn-group { margin-top: 15px; }");
                out.println("</style>");
                out.println("</head>");
                out.println("<body>");
                out.println("<div class='container'>");
                out.println("  <h2>❌ SKU验证失败</h2>");
                out.println("  <p class='message error'>仓库SKU <strong>" + warehouseSku + "</strong> 不存在于仓库系统中</p>");
                out.println("  <p class='message'>请检查输入或联系仓库管理员，<span id='countdown' class='countdown'>3</span>秒后自动返回编辑页</p>");
                out.println("  <div class='btn-group'>");
                out.println("    <a href='" + editUrl + "' class='btn btn-edit'>立即返回编辑</a>");
                // 构建列表页URL（带所有参数）
                String listUrl = buildListUrl(page, pageSize, searchKeyword, needEdit);
                out.println("    <a href='" + listUrl + "' class='btn btn-return'>返回列表页</a>");
                out.println("  </div>");
                out.println("</div>");
                
                // 3秒倒计时返回
                out.println("<script>");
                out.println("  let count = 3;");
                out.println("  const countdownElement = document.getElementById('countdown');");
                out.println("  const timer = setInterval(() => {");
                out.println("    count--;");
                out.println("    countdownElement.textContent = count;");
                out.println("    if (count <= 0) {");
                out.println("      clearInterval(timer);");
                out.println("      window.location.href = '" + editUrl + "';");
                out.println("    }");
                out.println("  }, 1000);");
                out.println("</script>");
                out.println("</body>");
                out.println("</html>");
                return;
            }

            // SKU存在 - 更新数据
            data.setWarehouseSku(warehouseSku);
            data.setUpdateUserId(currentUser.getId());
            boolean success = dao.updateNewData(data);

            if (success) {
                response.setContentType("text/html;charset=UTF-8");
                PrintWriter out = response.getWriter();
                
                // ✅ 构建列表页URL（带所有筛选参数）
                String listUrl = buildListUrl(page, pageSize, searchKeyword, needEdit);

                out.println("<!DOCTYPE html>");
                out.println("<html>");
                out.println("<head>");
                out.println("<meta charset='UTF-8'>");
                out.println("<title>操作成功</title>");
                out.println("<style>");
                out.println("body { font-family: 'Microsoft YaHei', Arial, sans-serif; text-align: center; padding: 50px 20px; background: #f5f5f5; }");
                out.println(".container { max-width: 500px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 4px 20px rgba(0,0,0,0.08); }");
                out.println("h2 { color: #67c23a; margin-bottom: 20px; }");
                out.println(".message { font-size: 18px; margin: 25px 0; }");
                out.println(".countdown { font-weight: bold; color: #409eff; }");
                out.println(".btn { display: inline-block; margin-top: 20px; padding: 10px 24px; background: #67c23a; color: white; text-decoration: none; border-radius: 4px; font-size: 16px; }");
                out.println(".btn:hover { background: #5daf34; }");
                out.println("</style>");
                out.println("</head>");
                out.println("<body>");
                out.println("<div class='container'>");
                out.println("  <h2>✅ 修改成功</h2>");
                out.println("  <p class='message'>数据已更新，<span id='countdown' class='countdown'>1</span>秒后自动返回列表页</p>");
                out.println("  <a href='" + listUrl + "' class='btn'>立即返回</a>");
                out.println("</div>");
                
                out.println("<script>");
                out.println("  let count = 1;");
                out.println("  const countdownElement = document.getElementById('countdown');");
                out.println("  const timer = setInterval(() => {");
                out.println("    count--;");
                out.println("    countdownElement.textContent = count;");
                out.println("    if (count <= 0) {");
                out.println("      clearInterval(timer);");
                out.println("      window.location.href = '" + listUrl + "';");
                out.println("    }");
                out.println("  }, 1000);");
                out.println("</script>");
                out.println("</body>");
                out.println("</html>");
            } else {
                redirectToEditPage(request, response, String.valueOf(id), "修改失败", page, pageSize, searchKeyword, needEdit);
            }

        } catch (Exception e) {
            e.printStackTrace();
            String idParam = request.getParameter("id");
            redirectToEditPage(request, response, idParam, "系统错误", page, pageSize, searchKeyword, needEdit);
        }
    }
    
    // ✅ 新增：构建列表URL的辅助方法（关键修复）
    private String buildListUrl(String page, String pageSize, String searchKeyword, String needEdit) {
        StringBuilder url = new StringBuilder("ListNewTableServlet?page=");
        url.append(page != null ? page : "1");
        url.append("&pageSize=").append(pageSize != null ? pageSize : "20");
        
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            try {
                url.append("&searchKeyword=").append(java.net.URLEncoder.encode(searchKeyword, "UTF-8"));
            } catch (Exception e) {
                // 忽略编码错误
            }
        }
        
        if (needEdit != null && !needEdit.isEmpty()) {
            url.append("&needEdit=").append(needEdit);
        }
        
        return url.toString();
    }
    
    // ✅ 修改方法签名，添加 pageSize, searchKeyword, needEdit 参数
    private void redirectToEditPage(HttpServletRequest request, 
                                   HttpServletResponse response, 
                                   String id, String message, 
                                   String page, String pageSize,
                                   String searchKeyword, String needEdit) 
            throws IOException {
        
        String redirectUrl = "EditNewTableServlet?id=" + id;
        redirectUrl += "&page=" + (page != null ? page : "1");
        redirectUrl += "&pageSize=" + (pageSize != null ? pageSize : "20");
        if (searchKeyword != null && !searchKeyword.isEmpty()) {
            redirectUrl += "&searchKeyword=" + java.net.URLEncoder.encode(searchKeyword, "UTF-8");
        }
        if (needEdit != null && !needEdit.isEmpty()) {
            redirectUrl += "&needEdit=" + needEdit;
        }
        redirectUrl += "&message=" + java.net.URLEncoder.encode(message, "UTF-8");
        response.sendRedirect(redirectUrl);
    }
}
