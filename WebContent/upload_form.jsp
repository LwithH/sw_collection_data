<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>上传新资源 - 下载中心</title>
    <style>
        body { font-family: Arial, sans-serif; max-width: 800px; margin: 0 auto; padding: 20px; background: #f5f5f5; }
        .container { background: white; padding: 30px; border-radius: 8px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        .header { text-align: center; margin-bottom: 25px; }
        .header h1 { color: #409eff; margin: 0; }
        .tabs { display: flex; justify-content: center; margin-bottom: 25px; }
        .tab-btn { padding: 10px 20px; margin: 0 10px; border: none; background: #eef5ff; color: #409eff; cursor: pointer; border-radius: 4px; font-weight: bold; }
        .tab-btn.active { background: #409eff; color: white; }
        .form-section { display: none; }
        .form-section.active { display: block; }
        .form-group { margin-bottom: 20px; }
        label { display: block; margin-bottom: 8px; font-weight: bold; color: #666; }
        input[type="text"], input[type="file"], input[type="date"], textarea, select {
            width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; box-sizing: border-box;
        }
        textarea { height: 80px; resize: vertical; }
        .btn { padding: 12px 20px; background: #409eff; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; }
        .btn:hover { background: #3390e0; }
        .tip { font-size: 12px; color: #999; margin-top: 5px; font-style: italic; }
        .back-link { display: inline-block; margin-top: 20px; color: #67c23a; text-decoration: none; }
        .back-link:hover { text-decoration: underline; }
        
        /* 日期范围样式 */
        .date-range-group { 
            display: none; 
            margin-top: 15px; 
            padding: 15px; 
            background: #f8f9fa; 
            border-radius: 4px; 
            border-left: 3px solid #409eff; 
        }
        .date-range-fields { 
            display: flex; 
            gap: 15px; 
            flex-wrap: wrap; 
        }
        .date-field { 
            flex: 1; 
            min-width: 200px; 
        }
        .checkbox-group { margin-bottom: 15px; }
        .checkbox-group label { 
            display: flex; 
            align-items: center; 
            cursor: pointer; 
            font-weight: normal; 
        }
        .checkbox-group input[type="checkbox"] { 
            margin-right: 8px; 
            width: 16px; 
            height: 16px; 
        }
        
        /* 日期字段提示样式 */
        .date-required { 
            color: #f56c6c; 
            font-size: 14px; 
            margin-top: 5px; 
        }
        
        /* 响应式调整 */
        @media (max-width: 600px) {
            .date-range-fields { flex-direction: column; }
            .date-field { min-width: 100%; }
        }
    </style>
    <script>
        function showTab(tabId) {
            document.querySelectorAll('.form-section').forEach(section => {
                section.classList.remove('active');
            });
            document.querySelectorAll('.tab-btn').forEach(btn => {
                btn.classList.remove('active');
            });
            document.getElementById(tabId).classList.add('active');
            document.querySelector(`.tab-btn[data-tab="${tabId}"]`).classList.add('active');
        }
        
        // 日期范围复选框切换
        function toggleDateRange() {
            const checkbox = document.getElementById('dateRangeEnabled');
            const dateRangeGroup = document.getElementById('dateRangeGroup');
            
            if (checkbox.checked) {
                dateRangeGroup.style.display = 'block';
            } else {
                dateRangeGroup.style.display = 'none';
            }
        }
        
        // 验证日期范围
        function validateDateRange() {
            const startDate = document.getElementById('startDate').value;
            const endDate = document.getElementById('endDate').value;
            
            if (startDate && endDate && startDate > endDate) {
                alert('结束日期不能早于开始日期！');
                return false;
            }
            return true;
        }
        
        // 页面加载初始化
        document.addEventListener('DOMContentLoaded', function() {
            // 初始化日期范围显示状态
            toggleDateRange();
            
            // 表单提交验证
            const tableForm = document.getElementById('tableForm');
            if (tableForm) {
                tableForm.addEventListener('submit', function(e) {
                    const dateRangeEnabled = document.getElementById('dateRangeEnabled').checked;
                    
                    if (dateRangeEnabled) {
                        const startDate = document.getElementById('startDate').value;
                        const endDate = document.getElementById('endDate').value;
                        
                        if (!startDate || !endDate) {
                            e.preventDefault();
                            alert('请选择完整的日期范围！');
                            return false;
                        }
                        
                        if (startDate > endDate) {
                            e.preventDefault();
                            alert('结束日期不能早于开始日期！');
                            return false;
                        }
                    }
                    
                    // 显示确认对话框
                    if (!confirm('确认导出数据？\n注意：大数据量可能导致导出时间较长。')) {
                        e.preventDefault();
                        return false;
                    }
                    
                    return true;
                });
            }
        });
    </script>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>上传新资源</h1>
            <p>支持本地文件上传或从数据库导出任意表</p>
        </div>

        <!-- 切换标签 -->
        <div class="tabs">
            <button class="tab-btn active" data-tab="localForm" onclick="showTab('localForm')">本地文件上传</button>
            <button class="tab-btn" data-tab="tableForm" onclick="showTab('tableForm')">数据库表导出</button>
        </div>

        <!-- 本地文件上传表单 -->
        <div id="localForm" class="form-section active">
            <form action="DownloadCenterAdminServlet" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="doUploadLocal">
                
                <div class="form-group">
                    <label>资源名称 <span style="color:#f56c6c">*</span></label>
                    <input type="text" name="name" required placeholder="例如：Temu 2024年Q2数据">
                </div>
                
                <div class="form-group">
                    <label>分类（可选）</label>
                    <input type="text" name="category" placeholder="例如：Temu辅助数据">
                    <div class="tip">用于在下载中心分组显示</div>
                </div>
                
                <div class="form-group">
                    <label>描述（可选）</label>
                    <textarea name="description" placeholder="简要说明资源内容..."></textarea>
                </div>
                
                <div class="form-group">
                    <label>选择文件 <span style="color:#f56c6c">*</span></label>
                    <input type="file" name="file" required accept=".xlsx,.xls,.csv,.zip">
                    <div class="tip">支持 .xlsx, .xls, .csv, .zip, .doc, .docx 格式，大小 ≤ 50MB</div>
                </div>
                
                <button type="submit" class="btn">上传文件</button>
            </form>
        </div>

        <!-- 数据库表导出表单 -->
        <div id="tableForm" class="form-section">
            <form action="DownloadCenterAdminServlet" method="post" id="exportTableForm">
                <input type="hidden" name="action" value="doExportTable">
                
                <div class="form-group">
                    <label>资源名称 <span style="color:#f56c6c">*</span></label>
                    <input type="text" name="name" required placeholder="例如：用户行为日志表">
                </div>
                
                <div class="form-group">
                    <label>数据库表名 <span style="color:#f56c6c">*</span></label>
                    <input type="text" name="tableName" required placeholder="输入表名（仅字母、数字、下划线）">
                    <div class="tip">系统将读取该表数据并生成文件</div>
                </div>
                
                <div class="form-group">
                    <label>分类（可选）</label>
                    <input type="text" name="category" placeholder="例如：原始日志">
                </div>
                
                <div class="form-group">
                    <label>描述（可选）</label>
                    <textarea name="description" placeholder="说明表结构或用途..."></textarea>
                </div>
                
                <!-- 导出格式选择 -->
                <div class="form-group">
                    <label>导出格式</label>
                    <label class="checkbox-group">
                        <input type="radio" name="fileType" value="csv" checked> CSV (.csv)
                    </label>
                    <label class="checkbox-group" style="margin-left:15px;">
                        <input type="radio" name="fileType" value="xlsx"> Excel (.xlsx)
                    </label>
                    <div class="tip">Excel 格式支持多工作表、样式，但生成稍慢</div>
                </div>
                
                <!-- 动态生成选项 -->
                <div class="form-group">
                    <label class="checkbox-group">
                        <input type="checkbox" name="isDynamic" value="true"> 
                        每次下载时生成最新数据（不保存静态文件）
                    </label>
                    <div class="tip">勾选后，用户下载将实时查询数据库，始终获取最新内容</div>
                </div>
                
                <!-- 日期范围导出选项 -->
                <div class="form-group">
                    <label class="checkbox-group">
                        <input type="checkbox" id="dateRangeEnabled" name="dateRangeEnabled" value="true" onchange="toggleDateRange()"> 
                        允许按日期范围导出
                    </label>
                    <div class="tip">勾选后，用户下载时可以指定日期范围（表必须包含日期字段）</div>
                </div>
                
                <!-- 日期范围选择（默认隐藏） -->
                <div id="dateRangeGroup" class="date-range-group">
                    <div style="margin-bottom: 10px; font-weight: bold; color: #409eff;">
                        <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" fill="currentColor" viewBox="0 0 16 16" style="vertical-align: middle; margin-right: 5px;">
                            <path d="M3.5 0a.5.5 0 0 1 .5.5V1h8V.5a.5.5 0 0 1 1 0V1h1a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V3a2 2 0 0 1 2-2h1V.5a.5.5 0 0 1 .5-.5zM2 2a1 1 0 0 0-1 1v11a1 1 0 0 0 1 1h12a1 1 0 0 0 1-1V3a1 1 0 0 0-1-1H2z"/>
                            <path d="M2.5 4a.5.5 0 0 1 .5-.5h10a.5.5 0 0 1 .5.5v1a.5.5 0 0 1-.5.5H3a.5.5 0 0 1-.5-.5V4z"/>
                        </svg>
                        设置默认日期范围（可选）
                    </div>
                    <div class="date-range-fields">
                        <div class="date-field">
                            <label>开始日期</label>
                            <input type="date" id="startDate" name="startDate">
                            <div class="tip">表中最小的可用日期</div>
                        </div>
                        <div class="date-field">
                            <label>结束日期</label>
                            <input type="date" id="endDate" name="endDate">
                            <div class="tip">表中最大的可用日期</div>
                        </div>
                    </div>
                    <div class="tip" style="margin-top: 10px;">
                        注意：指定的日期范围将在导出时作为默认值，用户下载时可以修改
                    </div>
                </div>
                
                <button type="submit" class="btn">导出数据</button>
            </form>
        </div>

        <a href="DownloadCenterAdminServlet?action=manage" class="back-link"> 返回资源管理</a>
    </div>
</body>
</html>