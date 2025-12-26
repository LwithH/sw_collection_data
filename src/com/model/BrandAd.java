package com.model;

public class BrandAd {
    private int id; // 主键（假设数据库有自增id）
    private String campaignName;
    private String sku;
    private String uacs;
    private String salesDepart;
    private int updateUseId; // 新增字段
    private String warehouseSku;
    private int quantity; // 新增数量字段
    private String uacId; // 注意：字段名建议用 uacId（驼峰），对应数据库 uac_id
    // 新增字段：sbsku1~sbsku5（String类型，统一保持一致）
    private String sbsku1;
    private String sbsku2;
    private String sbsku3;
    private String sbsku4;
    private String sbsku5;
    // 所有sbquantity字段统一改为Integer类型（允许为空，适配数据库空值场景）
    private Integer sbquantity1;
    private Integer sbquantity2;
    private Integer sbquantity3;
    private Integer sbquantity4;
    private Integer sbquantity5;
    private String sellerName;
    // 无参构造（保留不变）
    public BrandAd() {}

    // 全参构造（不含 id，保留原有参数，避免影响历史调用）
    public BrandAd(String campaignName, String sku, String uacs, String salesDepart) {
        this.campaignName = campaignName;
        this.sku = sku;
        this.uacs = uacs;
        this.salesDepart = salesDepart;
    }

    // 原有字段的 Getter 和 Setter（保留不变）
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCampaignName() { return campaignName; }
    public void setCampaignName(String campaignName) { this.campaignName = campaignName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getUacs() { return uacs; }
    public void setUacs(String uacs) { this.uacs = uacs; }

    public String getSalesDepart() { return salesDepart; }
    public void setSalesDepart(String salesDepart) { this.salesDepart = salesDepart; }

    public int getUpdateUseId() { return updateUseId; }
    public void setUpdateUseId(int updateUseId) { this.updateUseId = updateUseId; }

    public String getWarehouseSku() {
        return warehouseSku;
    }
    
    public void setWarehouseSku(String warehouseSku) {
        this.warehouseSku = warehouseSku;
    }

    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
    
    // sbsku1~sbsku5 的 Getter 和 Setter（String类型，无修改）
    public String getSbsku1() {
        return sbsku1;
    }
    
    public void setSbsku1(String sbsku1) {
        this.sbsku1 = sbsku1;
    }

    public String getSbsku2() {
        return sbsku2;
    }

    public void setSbsku2(String sbsku2) {
        this.sbsku2 = sbsku2;
    }

    public String getSbsku3() {
        return sbsku3;
    }

    public void setSbsku3(String sbsku3) {
        this.sbsku3 = sbsku3;
    }

    public String getSbsku4() {
        return sbsku4;
    }

    public void setSbsku4(String sbsku4) {
        this.sbsku4 = sbsku4;
    }

    public String getSbsku5() {
        return sbsku5;
    }

    public void setSbsku5(String sbsku5) {
        this.sbsku5 = sbsku5;
    }
    
    // 所有sbquantity字段（1~5）统一为Integer类型的 Getter 和 Setter
    public Integer getSbquantity1() {
        return sbquantity1;
    }
    
    public void setSbquantity1(Integer sbquantity1) {
        this.sbquantity1 = sbquantity1;
    }

    public Integer getSbquantity2() {
        return sbquantity2;
    }

    public void setSbquantity2(Integer sbquantity2) {
        this.sbquantity2 = sbquantity2;
    }

    public Integer getSbquantity3() {
        return sbquantity3;
    }

    public void setSbquantity3(Integer sbquantity3) {
        this.sbquantity3 = sbquantity3;
    }

    public Integer getSbquantity4() {
        return sbquantity4;
    }

    public void setSbquantity4(Integer sbquantity4) {
        this.sbquantity4 = sbquantity4;
    }

    public Integer getSbquantity5() {
        return sbquantity5;
    }

    public void setSbquantity5(Integer sbquantity5) {
        this.sbquantity5 = sbquantity5;
    }

    public String getUacId() {
        return uacId;
    }

    public void setUacId(String uacId) {
        this.uacId = uacId;
    }
    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }
}