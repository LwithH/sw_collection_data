package com.model;

public class AccountData {
    private int id;
    private String mains;
    private String accName;
    private int typeOpid;
    private int countryId;
    private int platformid;
    private int salesDepart;
    private String status;
    
    // 关联字段
    private String typeOp;
    private String country;
    private String platform;
    private String departName;

    private int areaId;
    private String area;
    private String ziniao;          // 紫鸟
    private String receiptStatus;   // 收款状态
    private String s1;              // 易仓名（新增必填字段）

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    
    public String getMains() { return mains; }
    public void setMains(String mains) { this.mains = mains; }
    
    public String getAccName() { return accName; }
    public void setAccName(String accName) { this.accName = accName; }
    
    public int getTypeOpid() { return typeOpid; }
    public void setTypeOpid(int typeOpid) { this.typeOpid = typeOpid; }
    
    public int getCountryId() { return countryId; }
    public void setCountryId(int countryId) { this.countryId = countryId; }
    
    public int getPlatformid() { return platformid; }
    public void setPlatformid(int platformid) { this.platformid = platformid; }
    
    public int getSalesDepart() { return salesDepart; }
    public void setSalesDepart(int salesDepart) { this.salesDepart = salesDepart; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getTypeOp() { return typeOp; }
    public void setTypeOp(String typeOp) { this.typeOp = typeOp; }
    
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
    
    public String getDepartName() { return departName; }
    public void setDepartName(String departName) { this.departName = departName; }
    
    public int getAreaId() { return areaId; }
    public void setAreaId(int areaId) { this.areaId = areaId; }
    
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    
    public String getZiniao() { return ziniao; }
    public void setZiniao(String ziniao) { this.ziniao = ziniao; }

    public String getReceiptStatus() { return receiptStatus; }
    public void setReceiptStatus(String receiptStatus) { this.receiptStatus = receiptStatus; }
    
    public String getS1() { return s1; }
    public void setS1(String s1) { this.s1 = s1; }
}
