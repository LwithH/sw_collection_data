package com.model;

import java.time.LocalDateTime;

public class OperationLog {
    private int id;
    private String operationType;
    private String operationContent;
    private String ipAddress;
    private String username;
    private LocalDateTime createTime; // 新增：自动记录日志创建时间
 // 在所有有参构造函数之后添加：
    public OperationLog() {
        // 默认构造函数，用于 JSP/DAO 反射赋值
    }

    // 构造函数（含所有字段）
    public OperationLog(String operationType, String operationContent, String ipAddress, String username) {
        this.operationType = operationType;
        this.operationContent = operationContent;
        this.ipAddress = ipAddress;
        this.username = username;
        // createTime 由数据库自动填充，此处可不设初始值
    }

    // 全参构造函数（建议保留）
    public OperationLog(int id, String operationType, String operationContent, 
                        String ipAddress, String username, LocalDateTime createTime) {
        this.id = id;
        this.operationType = operationType;
        this.operationContent = operationContent;
        this.ipAddress = ipAddress;
        this.username = username;
        this.createTime = createTime;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public String getOperationContent() {
        return operationContent;
    }

    public void setOperationContent(String operationContent) {
        this.operationContent = operationContent;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "OperationLog{" +
                "id=" + id +
                ", operationType='" + operationType + '\'' +
                ", operationContent='" + operationContent + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                ", username='" + username + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
