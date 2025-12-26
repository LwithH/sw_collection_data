package com.servlet;

import com.dao.AccountDAO;
import com.dao.OperationLogZhDAO;
import com.model.AccountData;
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

@WebServlet("/AddAccountServlet")
public class AddAccountServlet extends HttpServlet {
    
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
            
            // 获取参数
            String mains = json.has("mains") ? json.get("mains").getAsString() : "";
            String accName = json.has("acc_name") ? json.get("acc_name").getAsString() : "";
            String s1 = json.has("s1") ? json.get("s1").getAsString() : ""; // 新增：易仓名
            int typeOpid = json.has("type_opid") ? json.get("type_opid").getAsInt() : 0;
            
            // 国家/站点和区域二选一，允许为空
            int countryId = 0;
            if (json.has("country_id") && !json.get("country_id").getAsString().isEmpty()) {
                countryId = json.get("country_id").getAsInt();
            }
            
            int areaId = 0;
            if (json.has("area_id") && !json.get("area_id").getAsString().isEmpty()) {
                areaId = json.get("area_id").getAsInt();
            }
            
            int platformid = json.has("platformid") ? json.get("platformid").getAsInt() : 0;
            int salesDepart = json.has("sales_depart") ? json.get("sales_depart").getAsInt() : 0;
            String status = json.has("status") ? json.get("status").getAsString() : "1";
            String ziniao = json.has("ziniao") ? json.get("ziniao").getAsString() : "";
            
            // 验证必填字段
            if (mains == null || mains.trim().isEmpty()) {
                throw new IllegalArgumentException("主体简称不能为空");
            }
            if (accName == null || accName.trim().isEmpty()) {
                throw new IllegalArgumentException("店铺名称不能为空");
            }
            if (s1 == null || s1.trim().isEmpty()) { // 新增：易仓名验证
                throw new IllegalArgumentException("易仓名不能为空");
            }
            if (typeOpid == 0) {
                throw new IllegalArgumentException("请选择模式");
            }
            if (platformid == 0) {
                throw new IllegalArgumentException("请选择平台");
            }
            if (salesDepart == 0) {
                throw new IllegalArgumentException("请选择销售部门");
            }
            
            // 验证国家/站点和区域至少填写一个
            if (countryId == 0 && areaId == 0) {
                throw new IllegalArgumentException("国家/站点和区域至少填写一个");
            }
            
            // 创建 AccountData 对象
            AccountData account = new AccountData();
            account.setMains(mains);
            account.setAccName(accName);
            account.setS1(s1); // 设置易仓名
            account.setTypeOpid(typeOpid);
            account.setCountryId(countryId);
            account.setAreaId(areaId);
            account.setPlatformid(platformid);
            account.setSalesDepart(salesDepart);
            account.setStatus(status);
            account.setZiniao(ziniao);
            
            // 插入数据库
            AccountDAO dao = new AccountDAO();
            dao.insertAccount(account);
            
            // ✅ 记录日志
            User loginUser = (User) request.getSession().getAttribute("loginUser");
            if (loginUser != null) {
                OperationLogZh logZh = new OperationLogZh();
                logZh.setUserId(loginUser.getId());
                logZh.setUsername(loginUser.getUsername());
                logZh.setOperationType("INSERT");
                logZh.setOperationDesc("新增店铺：" + accName + "，主体简称：" + mains + "，易仓名：" + s1);
                logZh.setIpAddress(request.getRemoteAddr());
                new OperationLogZhDAO().insertLogZh(logZh);
            }
            
            // 返回成功
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "新增成功");
            out.write(new Gson().toJson(result));
            
        } catch (IllegalArgumentException e) {
            // 参数验证失败
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", e.getMessage());
            out.write(new Gson().toJson(result));
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "新增失败: " + e.getMessage());
            out.write(new Gson().toJson(result));
        } finally {
            out.close();
        }
    }
}
