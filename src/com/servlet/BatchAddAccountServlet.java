package com.servlet;

import com.dao.AccountDAO;
import com.dao.OperationLogZhDAO;
import com.model.AccountData;
import com.model.OperationLogZh;
import com.model.User;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@WebServlet("/BatchAddAccountServlet")
public class BatchAddAccountServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();

        try {
            // 读取 JSON 请求体
            String jsonBody = request.getReader().lines().collect(Collectors.joining());
            JsonObject root = JsonParser.parseString(jsonBody).getAsJsonObject();
            
            if (!root.has("accounts") || !root.get("accounts").isJsonArray()) {
                throw new IllegalArgumentException("请求格式错误：缺少 accounts 数组");
            }

            JsonArray accountsJson = root.getAsJsonArray("accounts");
            if (accountsJson.size() == 0) {
                throw new IllegalArgumentException("账号列表不能为空");
            }

            AccountDAO accountDAO = new AccountDAO();
            User loginUser = (User) request.getSession().getAttribute("loginUser");
            
            // 批量插入
            for (int i = 0; i < accountsJson.size(); i++) {
                JsonObject accJson = accountsJson.get(i).getAsJsonObject();
                
                AccountData account = new AccountData();
                account.setMains(getString(accJson, "mains", true));
                account.setAccName(getString(accJson, "acc_name", true));
                account.setZiniao(getString(accJson, "ziniao", true));
                account.setS1(getString(accJson, "s1", true)); // 易仓名必填
                
                account.setTypeOpid(getInt(accJson, "type_opid", true));
                account.setPlatformid(getInt(accJson, "platformid", true));
                account.setSalesDepart(getInt(accJson, "sales_depart", true));
                account.setStatus(getString(accJson, "status", false, "1"));
                
                // country_id 和 area_id 可为空（但批量时通常都有）
                if (accJson.has("country_id") && !accJson.get("country_id").isJsonNull() && 
                    !accJson.get("country_id").getAsString().trim().isEmpty()) {
                    account.setCountryId(accJson.get("country_id").getAsInt());
                } else {
                    account.setCountryId(0); // 0 表示 NULL
                }
                
                if (accJson.has("area_id") && !accJson.get("area_id").isJsonNull() && 
                    !accJson.get("area_id").getAsString().trim().isEmpty()) {
                    account.setAreaId(accJson.get("area_id").getAsInt());
                } else {
                    account.setAreaId(0);
                }

                // 插入数据库
                accountDAO.insertAccount(account);

                // 记录日志
                if (loginUser != null) {
                    OperationLogZh log = new OperationLogZh();
                    log.setUserId(loginUser.getId());
                    log.setUsername(loginUser.getUsername());
                    log.setOperationType("INSERT");
                    log.setOperationDesc("批量新增店铺：" + account.getAccName() + "（主体：" + account.getMains() + "）");
                    log.setIpAddress(request.getRemoteAddr());
                    new OperationLogZhDAO().insertLogZh(log);
                }
            }

            // 成功响应
            JsonObject result = new JsonObject();
            result.addProperty("success", true);
            result.addProperty("message", "成功新增 " + accountsJson.size() + " 个账号");
            out.write(new Gson().toJson(result));

        } catch (Exception e) {
            e.printStackTrace();
            JsonObject error = new JsonObject();
            error.addProperty("success", false);
            error.addProperty("message", "批量新增失败: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write(new Gson().toJson(error));
        } finally {
            out.close();
        }
    }

    // 工具方法：安全获取字符串（支持必填校验）
    private String getString(JsonObject obj, String key, boolean required) throws IllegalArgumentException {
        return getString(obj, key, required, null);
    }

    private String getString(JsonObject obj, String key, boolean required, String defaultValue) throws IllegalArgumentException {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            if (required) {
                throw new IllegalArgumentException("缺少必填字段: " + key);
            }
            return defaultValue;
        }
        String value = obj.get(key).getAsString().trim();
        if (required && value.isEmpty()) {
            throw new IllegalArgumentException("字段不能为空: " + key);
        }
        return value;
    }

    // 工具方法：安全获取整数
    private int getInt(JsonObject obj, String key, boolean required) throws IllegalArgumentException {
        if (!obj.has(key) || obj.get(key).isJsonNull()) {
            if (required) {
                throw new IllegalArgumentException("缺少必填字段: " + key);
            }
            return 0;
        }
        try {
            return obj.get(key).getAsInt();
        } catch (Exception e) {
            throw new IllegalArgumentException("字段格式错误（应为整数）: " + key);
        }
    }
}
