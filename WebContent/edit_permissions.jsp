<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="com.servlet.DownloadCenterAdminServlet" %>
<%@ page import="com.servlet.DownloadCenterAdminServlet.Resource" %>
<%@ page import="com.servlet.DownloadCenterAdminServlet.Permission" %>
<%@ page import="com.model.User" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>
<%
    // 部门映射关系
    Map<String, String> departmentMap = new java.util.HashMap<>();
    departmentMap.put("1", "销售一部");
    departmentMap.put("2", "销售二部");
    departmentMap.put("3", "乐器项目部");
    departmentMap.put("4", "大件项目部");
    departmentMap.put("5", "销售三部");
    departmentMap.put("8", "汽摩配项目部");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>编辑资源权限</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 1000px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { margin-bottom: 20px; border-bottom: 1px solid #eee; padding-bottom: 15px; }
        .header h1 { color: #409eff; margin: 0; }
        .resource-info { background: #f8f9fa; padding: 15px; border-radius: 5px; margin-bottom: 20px; }
        .resource-info h3 { margin-top: 0; color: #409eff; }
        .resource-meta { display: flex; flex-wrap: wrap; gap: 15px; margin-top: 10px; }
        .meta-item { flex: 1; min-width: 200px; }
        .meta-label { font-weight: bold; color: #666; margin-right: 5px; }
        
        .permissions-section { margin: 25px 0; }
        .section-title { font-size: 18px; margin-bottom: 15px; color: #333; border-left: 4px solid #409eff; padding-left: 10px; }
        
        .checkbox-group { max-height: 350px; overflow-y: auto; border: 1px solid #ddd; padding: 15px; border-radius: 5px; background: #f9f9f9; }
        .checkbox-item { margin: 10px 0; display: flex; align-items: center; }
        .checkbox-item input { margin-right: 10px; }
        .checkbox-item label { flex: 1; cursor: pointer; }
        
        .btn { padding: 10px 20px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer; text-decoration: none; display: inline-block; margin-right: 10px; }
        .btn:hover { background: #3390e0; }
        .btn-cancel { background: #909399; }
        .btn-cancel:hover { background: #7a7e83; }
        
        .current-permissions { margin: 20px 0; padding: 20px; background: #f0f9eb; border-radius: 5px; border: 1px solid #e1f3d8; }
        .current-permissions h4 { color: #67c23a; margin-top: 0; margin-bottom: 15px; }
        .permission-tag { display: inline-block; background: #e1f3d8; color: #67c23a; padding: 5px 10px; border-radius: 4px; margin: 5px; font-size: 13px; }
        .permission-dept { background: #eef5ff; color: #409eff; }
        .permission-user { background: #f0f4ff; color: #5a7ffb; }
        
        .form-actions { text-align: center; margin-top: 30px; }
        .tip-box { margin-top: 20px; padding: 15px; background: #f4f4f5; border-radius: 5px; text-align: center; color: #606266; font-size: 14px; }
        
        /* 部门组织结构样式 */
        .department-group { margin-bottom: 15px; padding: 10px; border: 1px solid #e4e7ed; border-radius: 5px; background: white; }
        .department-header { display: flex; align-items: center; margin-bottom: 8px; cursor: pointer; }
        .department-name { font-weight: bold; color: #409eff; flex: 1; }
        .select-all-btn { padding: 3px 8px; font-size: 12px; margin-right: 5px; cursor: pointer; }
        
        /* 折叠/展开箭头 */
        .toggle-arrow { 
            margin-right: 10px; 
            transition: transform 0.3s ease;
            font-size: 14px;
            color: #909399;
        }
        .toggle-arrow.expanded { transform: rotate(90deg); }
        
        /* 用户列表容器 - 初始折叠 */
        .user-list-container { 
            display: none;
            margin-top: 10px;
            padding: 10px;
            border-top: 1px dashed #e4e7ed;
        }
        .user-list-container.expanded { 
            display: block;
            animation: fadeIn 0.3s ease;
        }
        
        @keyframes fadeIn {
            from { opacity: 0; }
            to { opacity: 1; }
        }
        
        /* 展开/折叠控制按钮 */
        .collapse-controls { 
            text-align: right; 
            margin-bottom: 10px; 
            font-size: 13px;
        }
        .collapse-btn { 
            color: #409eff; 
            background: none; 
            border: 1px solid #409eff; 
            border-radius: 3px; 
            padding: 3px 8px; 
            margin-left: 5px;
            cursor: pointer;
            font-size: 12px;
        }
        .collapse-btn:hover { background: #409eff; color: white; }
        
        /* 响应式 */
        @media (max-width: 768px) {
            .resource-meta { flex-direction: column; }
            .meta-item { min-width: 100%; }
            .department-header { flex-wrap: wrap; }
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>编辑资源权限</h1>
            <p>设置哪些部门或用户可以查看此资源</p>
        </div>

        <%
            Resource resource = (Resource) request.getAttribute("resource");
            List<Permission> permissions = (List<Permission>) request.getAttribute("permissions");
            List<String> departmentNumbers = (List<String>) request.getAttribute("departments");
            List<User> allUsers = (List<User>) request.getAttribute("allUsers");
            
            if (resource == null) {
        %>
            <div style="text-align: center; color: #f56c6c; padding: 40px;">资源不存在</div>
        <%
            } else {
                // 按部门分组用户
                Map<String, List<User>> usersByDepartment = new java.util.HashMap<>();
                if (allUsers != null) {
                    for (User user : allUsers) {
                        String dept = user.getUserDepart() != null ? user.getUserDepart() : "未分组";
                        List<User> userList = usersByDepartment.get(dept);
                        if (userList == null) {
                            userList = new java.util.ArrayList<>();
                            usersByDepartment.put(dept, userList);
                        }
                        userList.add(user);
                    }
                }
        %>
        <div class="resource-info">
            <h3><%= resource.getName() %></h3>
            <div class="resource-meta">
                <div class="meta-item">
                    <span class="meta-label">分类：</span>
                    <%= resource.getCategory() != null ? resource.getCategory() : "-" %>
                </div>
                <div class="meta-item">
                    <span class="meta-label">文件类型：</span>
                    <%= resource.getFileType() != null ? resource.getFileType().toUpperCase() : "-" %>
                </div>
                <div class="meta-item">
                    <span class="meta-label">上传时间：</span>
                    <%= resource.getUploadTime() != null ? resource.getUploadTime().toString().substring(0, 19) : "-" %>
                </div>
                <div class="meta-item">
                    <span class="meta-label">上传人：</span>
                    <%= resource.getUploaderName() %>
                </div>
            </div>
            <div style="margin-top: 10px;">
                <span class="meta-label">描述：</span>
                <%= resource.getDescription() != null ? resource.getDescription() : "无描述" %>
            </div>
        </div>

        <!-- 显示当前权限 -->
        <div class="current-permissions">
            <h4>当前权限设置</h4>
            <%
                if (permissions == null || permissions.isEmpty()) {
            %>
                <p style="color: #999; font-style: italic; text-align: center;">未设置权限（所有人可见）</p>
            <%
                } else {
                    int deptCount = 0;
                    int userCount = 0;
                    
                    // 先显示部门权限
                    for (Permission p : permissions) {
                        if ("DEPARTMENT".equals(p.getTargetType())) {
                            String deptNum = p.getTargetValue();
                            String deptName = departmentMap.containsKey(deptNum) ? 
                                              departmentMap.get(deptNum) : 
                                              "未知部门(" + deptNum + ")";
            %>
                <span class="permission-tag permission-dept" title="部门：<%= deptName %>">
                    部门：<%= deptName %>
                </span>
            <%
                            deptCount++;
                        }
                    }
                    
                    // 再显示用户权限
                    for (Permission p : permissions) {
                        if ("USER".equals(p.getTargetType())) {
                            String userId = p.getTargetValue();
                            String userName = "ID:" + userId;
                            // 查找用户名
                            if (allUsers != null) {
                                for (User u : allUsers) {
                                    if (String.valueOf(u.getId()).equals(userId)) {
                                        userName = u.getName() != null ? u.getName() : u.getUsername();
                                        break;
                                    }
                                }
                            }
            %>
                <span class="permission-tag permission-user" title="用户：<%= userName %>">
                    用户：<%= userName %>
                </span>
            <%
                            userCount++;
                        }
                    }
            %>
            <div style="margin-top: 10px; font-size: 12px; color: #606266;">
                <span>部门权限：<%= deptCount %>个</span>
                <span style="margin-left: 15px;">用户权限：<%= userCount %>个</span>
                <span style="margin-left: 15px;">总计：<%= permissions.size() %>个权限</span>
            </div>
            <%
                }
            %>
        </div>

        <form action="DownloadCenterAdminServlet?action=savePermissions" method="post">
            <input type="hidden" name="resourceId" value="<%= resource.getId() %>">
            
            <div class="permissions-section">
                <div class="section-title">
                    部门权限
                    <button type="button" onclick="toggleAllDepartments(true)" 
                            style="padding: 4px 10px; font-size: 12px; margin-left: 10px; background: #67c23a; color: white; border: none; border-radius: 3px;">全选</button>
                    <button type="button" onclick="toggleAllDepartments(false)" 
                            style="padding: 4px 10px; font-size: 12px; margin-left: 5px; background: #f56c6c; color: white; border: none; border-radius: 3px;">全不选</button>
                </div>
                <div class="checkbox-group">
                    <%
                        if (departmentNumbers != null && !departmentNumbers.isEmpty()) {
                            for (String deptNum : departmentNumbers) {
                                String deptName = departmentMap.containsKey(deptNum) ? 
                                                  departmentMap.get(deptNum) : 
                                                  "未知部门(" + deptNum + ")";
                                
                                boolean checked = false;
                                if (permissions != null) {
                                    for (Permission p : permissions) {
                                        if ("DEPARTMENT".equals(p.getTargetType()) && deptNum.equals(p.getTargetValue())) {
                                            checked = true;
                                            break;
                                        }
                                    }
                                }
                    %>
                    <div class="checkbox-item">
                        <input type="checkbox" name="allowed_departments[]" value="<%= deptNum %>" 
                               id="dept_<%= deptNum %>" <%= checked ? "checked" : "" %> class="dept-checkbox">
                        <label for="dept_<%= deptNum %>">
                            <strong><%= deptName %></strong>
                            <span style="color: #666; font-size: 12px; margin-left: 5px;">(编号: <%= deptNum %>)</span>
                        </label>
                    </div>
                    <%
                            }
                        } else {
                    %>
                    <p style="color: #999; font-style: italic; text-align: center;">暂无部门数据</p>
                    <%
                        }
                    %>
                </div>
            </div>

            <div class="permissions-section">
                <div class="section-title">
                    用户权限（按部门分组）
                    <button type="button" onclick="toggleAllUsers(true)" 
                            style="padding: 4px 10px; font-size: 12px; margin-left: 10px; background: #67c23a; color: white; border: none; border-radius: 3px;">全选</button>
                    <button type="button" onclick="toggleAllUsers(false)" 
                            style="padding: 4px 10px; font-size: 12px; margin-left: 5px; background: #f56c6c; color: white; border: none; border-radius: 3px;">全不选</button>
                </div>
                
                <!-- 展开/折叠控制 -->
                <div class="collapse-controls">
                    <span>展开/折叠控制：</span>
                    <button type="button" class="collapse-btn" onclick="expandAllDepartments()">展开所有</button>
                    <button type="button" class="collapse-btn" onclick="collapseAllDepartments()">折叠所有</button>
                </div>
                
                <%
                    if (usersByDepartment != null && !usersByDepartment.isEmpty()) {
                        // 按部门数字排序
                        List<String> sortedDeptKeys = new java.util.ArrayList<>(usersByDepartment.keySet());
                        java.util.Collections.sort(sortedDeptKeys);
                        
                        for (String deptNum : sortedDeptKeys) {
                            List<User> deptUsers = usersByDepartment.get(deptNum);
                            if (deptUsers != null && !deptUsers.isEmpty()) {
                                String deptName = departmentMap.containsKey(deptNum) ? 
                                                  departmentMap.get(deptNum) : 
                                                  "未知部门(" + deptNum + ")";
                %>
                <div class="department-group" data-dept="<%= deptNum %>">
                    <div class="department-header">
                        <span class="toggle-arrow">▶</span>
                        <span class="department-name">
                            <%= deptName %> 
                            <span style="color: #666; font-size: 12px;">(共<%= deptUsers.size() %>人)</span>
                        </span>
                        <div>
                            <button type="button" onclick="toggleAllDepartmentUsers('<%= deptNum %>', true); event.stopPropagation();" 
                                    class="select-all-btn" style="background: #e1f3d8; color: #67c23a; border: 1px solid #67c23a;">全选</button>
                            <button type="button" onclick="toggleAllDepartmentUsers('<%= deptNum %>', false); event.stopPropagation();" 
                                    class="select-all-btn" style="background: #fde2e2; color: #f56c6c; border: 1px solid #f56c6c;">全不选</button>
                        </div>
                    </div>
                    <div class="user-list-container" id="user-list-<%= deptNum %>">
                        <div style="display: grid; grid-template-columns: repeat(auto-fill, minmax(250px, 1fr)); gap: 10px;">
                            <%
                                for (User u : deptUsers) {
                                    boolean checked = false;
                                    if (permissions != null) {
                                        for (Permission p : permissions) {
                                            if ("USER".equals(p.getTargetType()) && String.valueOf(u.getId()).equals(p.getTargetValue())) {
                                                checked = true;
                                                break;
                                            }
                                        }
                                    }
                                    
                                    String displayName = u.getName() != null && !u.getName().trim().isEmpty() ? 
                                                        u.getName() : 
                                                        (u.getUsername() != null ? u.getUsername() : "未知用户");
                            %>
                            <div class="checkbox-item">
                                <input type="checkbox" name="allowed_users[]" value="<%= u.getId() %>" 
                                       id="user_<%= u.getId() %>" <%= checked ? "checked" : "" %> 
                                       class="user-checkbox user-checkbox-<%= deptNum %>">
                                <label for="user_<%= u.getId() %>">
                                    <%= displayName %>
                                    <span style="color: #666; font-size: 12px; display: block; margin-top: 2px;">
                                        用户名: <%= u.getUsername() != null ? u.getUsername() : "-" %> | 
                                        用户ID: <%= u.getId() %>
                                    </span>
                                </label>
                            </div>
                            <%
                                }
                            %>
                        </div>
                    </div>
                </div>
                <%
                            }
                        }
                    } else {
                %>
                <div class="checkbox-group">
                    <p style="color: #999; font-style: italic; text-align: center;">暂无用户数据</p>
                </div>
                <%
                    }
                %>
            </div>

            <div class="form-actions">
                <button type="submit" class="btn">保存权限设置</button>
                <a href="DownloadCenterAdminServlet?action=manage" class="btn btn-cancel">取消并返回</a>
            </div>
            
            <div class="tip-box">
                <p><strong>提示：</strong></p>
                <ul style="text-align: left; margin: 10px auto; max-width: 600px;">
                    <li>如果不选择任何权限，则资源对<strong>所有人</strong>可见</li>
                    <li>点击部门名称可以展开/折叠该部门下的用户列表</li>
                    <li>可以选择整个部门（部门内所有用户自动拥有权限）</li>
                    <li>也可以选择特定用户（只有选中的用户有权限）</li>
                    <li>部门权限和用户权限可以<strong>同时设置</strong>，用户只需满足其中一项即可查看</li>
                    <li>已设置权限的用户：<span class="permission-tag permission-user">用户标签</span></li>
                    <li>已设置权限的部门：<span class="permission-tag permission-dept">部门标签</span></li>
                </ul>
            </div>
        </form>
        <%
            }
        %>
    </div>
    
    <script>
        // 页面加载完成后执行
        document.addEventListener('DOMContentLoaded', function() {
            // 绑定部门头部的点击事件
            const departmentHeaders = document.querySelectorAll('.department-header');
            departmentHeaders.forEach(header => {
                // 使用事件委托，只处理非按钮的点击
                header.addEventListener('click', function(event) {
                    // 如果点击的是按钮，不触发展开/折叠
                    if (event.target.tagName === 'BUTTON' || event.target.tagName === 'INPUT') {
                        return;
                    }
                    
                    const departmentGroup = this.closest('.department-group');
                    const deptNum = departmentGroup.getAttribute('data-dept');
                    
                    if (deptNum) {
                        toggleDepartment(deptNum);
                    }
                });
            });
            
            console.log('页面加载完成，部门点击事件已绑定');
        });
        
        // 切换单个部门的展开/折叠状态
        function toggleDepartment(deptNum) {
            console.log('切换部门:', deptNum);
            
            const container = document.getElementById('user-list-' + deptNum);
            const arrow = document.querySelector('.department-group[data-dept="' + deptNum + '"] .toggle-arrow');
            
            if (container && arrow) {
                // 切换展开状态
                if (container.classList.contains('expanded')) {
                    // 如果已展开，则折叠
                    container.classList.remove('expanded');
                    arrow.classList.remove('expanded');
                    arrow.textContent = '▶';
                } else {
                    // 如果已折叠，则展开
                    container.classList.add('expanded');
                    arrow.classList.add('expanded');
                    arrow.textContent = '▼';
                }
            } else {
                console.error('找不到元素: user-list-' + deptNum);
            }
        }
        
        // 展开单个部门
        function expandDepartment(deptNum) {
            const container = document.getElementById('user-list-' + deptNum);
            const arrow = document.querySelector('.department-group[data-dept="' + deptNum + '"] .toggle-arrow');
            
            if (container && arrow) {
                container.classList.add('expanded');
                arrow.classList.add('expanded');
                arrow.textContent = '▼';
            }
        }
        
        // 折叠单个部门
        function collapseDepartment(deptNum) {
            const container = document.getElementById('user-list-' + deptNum);
            const arrow = document.querySelector('.department-group[data-dept="' + deptNum + '"] .toggle-arrow');
            
            if (container && arrow) {
                container.classList.remove('expanded');
                arrow.classList.remove('expanded');
                arrow.textContent = '▶';
            }
        }
        
        // 展开所有部门
        function expandAllDepartments() {
            const deptGroups = document.querySelectorAll('.department-group');
            deptGroups.forEach(group => {
                const deptNum = group.getAttribute('data-dept');
                expandDepartment(deptNum);
            });
        }
        
        // 折叠所有部门
        function collapseAllDepartments() {
            const deptGroups = document.querySelectorAll('.department-group');
            deptGroups.forEach(group => {
                const deptNum = group.getAttribute('data-dept');
                collapseDepartment(deptNum);
            });
        }
        
        // 全选/全不选部门内用户
        function toggleAllDepartmentUsers(departmentId, checked) {
            const checkboxes = document.querySelectorAll('.user-checkbox-' + departmentId);
            checkboxes.forEach(cb => {
                cb.checked = checked;
            });
        }
        
        // 全选/全不选所有用户
        function toggleAllUsers(checked) {
            const checkboxes = document.querySelectorAll('.user-checkbox');
            checkboxes.forEach(cb => {
                cb.checked = checked;
            });
        }
        
        // 全选/全不选所有部门
        function toggleAllDepartments(checked) {
            const checkboxes = document.querySelectorAll('.dept-checkbox');
            checkboxes.forEach(cb => {
                cb.checked = checked;
            });
        }
    </script>
</body>
</html>