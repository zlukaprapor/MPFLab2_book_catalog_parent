package com.bookapp.core.domain;

public class PageRequest {
    private final int page;
    private final int size;
    private final String sort;

    public PageRequest(int page, int size, String sort) {
        this.page = Math.max(0, page);
        this.size = Math.min(100, Math.max(1, size));
        this.sort = sort != null ? sort : "title";
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getSort() {
        return sort;
    }

    public int getOffset() {
        return page * size;
    }
}