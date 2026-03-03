package com.spring.app.product.domain;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class CategoryDTO {

	private Long categoryNo;     // 카테고리번호 (PK)
    private String categoryName; // 카테고리명
}
