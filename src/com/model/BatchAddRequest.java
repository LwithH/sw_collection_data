// com/model/BatchAddRequest.java
package com.model;

public class BatchAddRequest {
    private String prefix;
    private String sku;
    private String seller;
    private String warehouseSku;
    private String asin;
    private String parentAsin;
    private String lifecl;

    // 无参构造器
    public BatchAddRequest() {}

    // getter 和 setter 方法
    public String getPrefix() { return prefix; }
    public void setPrefix(String prefix) { this.prefix = prefix; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getSeller() { return seller; }
    public void setSeller(String seller) { this.seller = seller; }

    public String getWarehouseSku() { return warehouseSku; }
    public void setWarehouseSku(String warehouseSku) { this.warehouseSku = warehouseSku; }

    public String getAsin() { return asin; }
    public void setAsin(String asin) { this.asin = asin; }

    public String getParentAsin() { return parentAsin; }
    public void setParentAsin(String parentAsin) { this.parentAsin = parentAsin; }

    public String getLifecl() { return lifecl; }
    public void setLifecl(String lifecl) { this.lifecl = lifecl; }
}