package com.qoocca.teachers.common.global.common;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponse<T> {
    private final List<T> content;
    private final long totalCount;
    private final int currentPage;
    private final int totalPage;

    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.totalCount = page.getTotalElements();
        this.currentPage = page.getNumber() + 1;
        this.totalPage = page.getTotalPages();
    }
}