package com.servlet;

import com.dao.AccountDAO;
import com.dao.OperationLogZhDAO;
import com.model.OperationLogZh;
import com.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/UpdateTypeOpServlet")
public class UpdateTypeOpServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        try {
            // 读取 JSON 请求体
            String jsonBody = request.getReader().lines().collect(Collectors.joining());
            JsonObject json = JsonParser.parseString(jsonBody).getAsJsonObject();

            int accountId = json.get("accountId").getAsInt();
            int newTypeOpid = json.get("typeOpid").getAsInt();

            // 更新数据库
            AccountDAO dao = new AccountDAO();
            dao.updateTypeOp(accountId, newTypeOpid);

            // ✅ 记录日志
            User loginUser = (User) request.getSession().getAttribute("loginUser");
            if (loginUser != null) {
                OperationLogZh logZh = new OperationLogZh();
                logZh.setUserId(loginUser.getId());
                logZh.setUsername(loginUser.getUsername());
                logZh.setOperationType("UPDATE");
                logZh.setOperationDesc("修改店铺ID=" + accountId + " 的模式为: " + newTypeOpid);
                logZh.setIpAddress(request.getRemoteAddr());
                new OperationLogZhDAO().insertLogZh(logZh);
            }

            // 返回成功
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "模式更新成功");
            out.write(new Gson().toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "更新失败: " + e.getMessage());
            out.write(new Gson().toJson(result));
        } finally {
            out.close();
        }
    }
}
