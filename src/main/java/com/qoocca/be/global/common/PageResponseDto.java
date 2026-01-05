package com.qoocca.be.global.common;

import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
public class PageResponseDto<T> {
    private List<T> content;
    private long totalCount;
    private int currentPage;
    private int totalPage;

    public PageResponseDto(Page<T> page) {
        this.content = page.getContent();
        this.totalCount = page.getTotalElements();
        this.currentPage = page.getNumber() + 1;
        this.totalPage = page.getTotalPages();
    }
}