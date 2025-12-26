package com.util;

public class PageUtil {
    private int currentPage;
    private int pageSize;
    private int total;
    private int totalPages;

    public PageUtil(int currentPage, int pageSize, int total) {
        this.currentPage = currentPage;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = (total + pageSize - 1) / pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotal() {
        return total;
    }

    public int getTotalPages() {
        return totalPages;
    }
}
