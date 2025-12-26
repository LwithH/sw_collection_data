package com.model;

import java.sql.Timestamp;

/**
 * 采集数据实体类，兼容旧表 temu		 和新表 福来
 */
public class CollectionData {
    // ========== 旧表字段（collection_data）==========
    private int id;
    private String sku;
    private String seller;
    private String isc1;
    private int createUserId;
    private String salesDepart;
    private String userOrganization;
    private int updateUserId;
    private String userIdDing;
    private Timestamp updateTime;

    // ========== 新表字段（collection_new）==========
    private String account;          // 账号
    private String campaignName;     // 广告系列名称（对应数据库 `Campaign name`）
    private String amount;           // 金额
    private String currency;         // 币种
    private String accountId;        // 账户ID
    private String warehouseSku;     // 仓库SKU
    private String spu;              // SPU

    // ========== 构造方法 ==========

    private String uacId; // 或 Integer，根据实际类型

    // Getter


    public String getUacId() {
        return uacId;
    }

    // Setter


    public void setUacId(String uacId) {
        this.uacId = uacId;
    }

    public CollectionData() {}

    public CollectionData(String sku, String seller, String isc1) {
        this.sku = sku;
        this.seller = seller;
        this.isc1 = isc1;
    }

    public CollectionData(String sku, String seller, String isc1, int createUserId) {
        this(sku, seller, isc1);
        this.createUserId = createUserId;
    }

    // ========== 旧表字段 getter/setter ==========

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getIsc1() {
        return isc1;
    }

    public void setIsc1(String isc1) {
        this.isc1 = isc1;
    }

    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    public String getSalesDepart() {
        return salesDepart;
    }

    public void setSalesDepart(String salesDepart) {
        this.salesDepart = salesDepart;
    }

    public String getUserOrganization() {
        return userOrganization;
    }

    public void setUserOrganization(String userOrganization) {
        this.userOrganization = userOrganization;
    }

    public int getUpdateUserId() {
        return updateUserId;
    }

    public void setUpdateUserId(int updateUserId) {
        this.updateUserId = updateUserId;
    }

    public String getUserIdDing() {
        return userIdDing;
    }

    public void setUserIdDing(String userIdDing) {
        this.userIdDing = userIdDing;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    // ========== 新表字段 getter/setter ==========

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getCampaignName() {
        return campaignName;
    }

    public void setCampaignName(String campaignName) {
        this.campaignName = campaignName;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getWarehouseSku() {
        return warehouseSku;
    }

    public void setWarehouseSku(String warehouseSku) {
        this.warehouseSku = warehouseSku;
    }

    public String getSpu() {
        return spu;
    }

    public void setSpu(String spu) {
        this.spu = spu;
    }
}
