package com.model;

public class AmazonData {
    private int id;
    private String uacs;  // 替代原marketId
    private String sku;
    private String seller;
    private String salesDepart;     // sales_depart
    private String title;  // 替代原userOrganization
    private String asin;
    private String parentAsin;
    private String lifecl;
    private String warehouseSku;    // 新增：仓库SKU
    private java.sql.Date skuLastDate; // 新增：用于年份筛选
    private String isc1;  // 新增：首位/接手标识字段
    private String user_id_ding; // 新增：用于存储卖家对应的用户ID
    private int createUserId; // 新增：创建者ID
    private int updateUserId; // 新增：最后更新者ID
    
    // ===== 新增的市场相关字段 =====
    private String marketId; // 从g_account表获取
    private String uacId;    // 从g_account表获取
    
    // 构造器
    public AmazonData() {}

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUacs() {
        return uacs;
    }
    public void setUacs(String uacs) {
        this.uacs = uacs;
    }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getSeller() { return seller; }
    public void setSeller(String seller) { this.seller = seller; }

    public String getSalesDepart() { return salesDepart; }
    public void setSalesDepart(String salesDepart) { this.salesDepart = salesDepart; }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    // 新增字段的 getter/setter
    public String getAsin() {
        return asin;
    }

    public void setAsin(String asin) {
        this.asin = asin;
    }

    public String getParentAsin() {
        return parentAsin;
    }

    public void setParentAsin(String parentAsin) {
        this.parentAsin = parentAsin;
    }

    public String getLifecl() {
        return lifecl;
    }

    public void setLifecl(String lifecl) {
        this.lifecl = lifecl;
    }
    
    // ===== 新增：仓库SKU =====
    public String getWarehouseSku() {
        return warehouseSku;
    }

    public void setWarehouseSku(String warehouseSku) {
        this.warehouseSku = warehouseSku;
    }
    
    public java.sql.Date getSkuLastDate() { return skuLastDate; }
    public void setSkuLastDate(java.sql.Date skuLastDate) { this.skuLastDate = skuLastDate; }
    
    public String getIsc1() { return isc1; }
    public void setIsc1(String isc1) { this.isc1 = isc1; }
    
    // 新增 user_id_ding 字段的 getter/setter
    public String getUser_id_ding() {
        return user_id_ding;
    }
    
    public void setUser_id_ding(String user_id_ding) {
        this.user_id_ding = user_id_ding;
    }
    
    // ===== 新增的字段 =====
    public int getCreateUserId() {
        return createUserId;
    }
    
    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }
    
    public int getUpdateUserId() {
        return updateUserId;
    }
    
    public void setUpdateUserId(int updateUserId) {
        this.updateUserId = updateUserId;
    }
    
    // ===== 新增的市场相关字段的 getter/setter =====
    public String getMarketId() {
        return marketId;
    }
    
    public void setMarketId(String marketId) {
        this.marketId = marketId;
    }
    
    public String getUacId() {
        return uacId;
    }
    
    public void setUacId(String uacId) {
        this.uacId = uacId;
    }
    
}
