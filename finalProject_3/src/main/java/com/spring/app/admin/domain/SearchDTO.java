package com.spring.app.admin.domain;

import lombok.Data;

@Data
public class SearchDTO {
    private String keyword; // 검색어
    private int count;      // 검색 횟수
}