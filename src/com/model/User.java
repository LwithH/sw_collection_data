package com.model;

public class User {
    private int id;
    private String username;
    private String password;
    private String isSeeAmazon;
    private String isSeeAccount;
    private String isSeeTemu;
    private String isSeeFulai;
    private String isSeeAd;
    private String userDepart;
    private String userIdDing;
    private String isAdmin;          // 通用管理员
    private String isDownloadAdmin;  // 下载中心管理员
    private String name;  // 新增字段
    private String isSeeDsp;
    // 无参构造
    public User() {}

    // 登录用
    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // 完整构造（用于数据库映射）
    public User(int id, String username, String password,
                String isSeeAmazon, String isSeeAccount,
                String isSeeTemu, String isSeeFulai, String isSeeAd,
                String userDepart, String userIdDing,
                String isAdmin, String isDownloadAdmin) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.isSeeAmazon = isSeeAmazon;
        this.isSeeAccount = isSeeAccount;
        this.isSeeTemu = isSeeTemu;
        this.isSeeFulai = isSeeFulai;
        this.isSeeAd = isSeeAd;
        this.userDepart = userDepart;
        this.userIdDing = userIdDing;
        this.isAdmin = isAdmin;
        this.isDownloadAdmin = isDownloadAdmin;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getIsSeeAmazon() { return isSeeAmazon; }
    public void setIsSeeAmazon(String isSeeAmazon) { this.isSeeAmazon = isSeeAmazon; }

    public String getIsSeeAccount() { return isSeeAccount; }
    public void setIsSeeAccount(String isSeeAccount) { this.isSeeAccount = isSeeAccount; }

    public String getIsSeeTemu() { return isSeeTemu; }
    public void setIsSeeTemu(String isSeeTemu) { this.isSeeTemu = isSeeTemu; }

    public String getIsSeeFulai() { return isSeeFulai; }
    public void setIsSeeFulai(String isSeeFulai) { this.isSeeFulai = isSeeFulai; }

    public String getIsSeeAd() { return isSeeAd; }
    public void setIsSeeAd(String isSeeAd) { this.isSeeAd = isSeeAd; }

    public String getUserDepart() { return userDepart; }
    public void setUserDepart(String userDepart) { this.userDepart = userDepart; }

    public String getUserIdDing() { return userIdDing; }
    public void setUserIdDing(String userIdDing) { this.userIdDing = userIdDing; }

    public String getIsAdmin() { return isAdmin; }
    public void setIsAdmin(String isAdmin) { this.isAdmin = isAdmin; }

    public String getIsDownloadAdmin() { return isDownloadAdmin; }
    public void setIsDownloadAdmin(String isDownloadAdmin) { this.isDownloadAdmin = isDownloadAdmin; }

    // 构造器、getter和setter
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    public String getIsSeeDsp() { return isSeeDsp; }
    public void setIsSeeDsp(String isSeeDsp) { this.isSeeDsp = isSeeDsp; }
}
