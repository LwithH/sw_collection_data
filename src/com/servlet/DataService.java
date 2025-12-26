package com.servlet;//temu

import java.util.List;

import com.dao.CollectionDAO;
import com.model.CollectionData;

/**
 * 数据服务层 - 作为DAO层和Servlet层之间的桥梁
 * 避免Servlet直接调用DAO，提高代码可维护性
 */
public class DataService {
    
    private CollectionDAO collectionDAO;
    
    public DataService() {
        this.collectionDAO = new CollectionDAO();
    }
    
    /**
     * 搜索所有匹配的数据（不分页，用于导出）
     * @param keyword 搜索关键词（SKU、Seller或ISC1）
     * @return 匹配的所有数据列表
     */
    public List<CollectionData> searchAllData(String keyword) {
        return collectionDAO.searchAllData(keyword);
    }
    
    /**
     * 获取所有Seller为空的数据（不分页）
     * @param keyword 搜索关键词
     * @return 所有Seller为空的匹配数据
     */
    public List<CollectionData> getEmptySellerDataAll(String keyword) {
        return collectionDAO.getEmptySellerDataAll(keyword);
    }
}
