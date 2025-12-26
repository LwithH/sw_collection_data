package com.servlet;
//账号系统
import com.dao.AccountDAO;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser; 
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/UpdateSystemServlet")
public class UpdateSystemServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        // 设置响应类型为 JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 读取请求体（JSON 字符串）
            StringBuilder jsonBuffer = new StringBuilder();
            String line;
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
            String jsonString = jsonBuffer.toString();

            // 解析 JSON
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            // 验证 accountId 是否存在
            if (!jsonObject.has("accountId")) {
                out.write(new Gson().toJson(new Response(false, "缺少账号ID")));
                return;
            }

            int accountId = jsonObject.get("accountId").getAsInt();
            String[] sValues = new String[20];

            // 提取 s1 ~ s20 字段，若不存在则设为空字符串
            for (int i = 1; i <= 20; i++) {
                String key = "s" + i;
                if (jsonObject.has(key) && !jsonObject.get(key).isJsonNull()) {
                    sValues[i - 1] = jsonObject.get(key).getAsString();
                } else {
                    sValues[i - 1] = ""; // 默认空字符串
                }
            }

            // 调用 DAO 更新数据库
            AccountDAO accountDAO = new AccountDAO();
            accountDAO.updateSystemFields(accountId, sValues);

            // 返回成功响应
            out.write(new Gson().toJson(new Response(true, "系统字段更新成功")));

        } catch (NumberFormatException e) {
            e.printStackTrace();
            out.write(new Gson().toJson(new Response(false, "账号ID格式错误")));
        } catch (Exception e) {
            e.printStackTrace();
            out.write(new Gson().toJson(new Response(false, "服务器内部错误: " + e.getMessage())));
        } finally {
            out.close();
        }
    }

    // 内部类：用于统一 JSON 响应格式
    private static class Response {
        private boolean success;
        private String message;

        public Response(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        // Gson 会自动序列化 public 字段或通过 getter
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
