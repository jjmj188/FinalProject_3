package com.spring.app.product.domain;

import lombok.Data;

@Data
public class ProductImageDTO {

	private Integer prdImgNo;     // 이미지번호(PK)
	private Integer productNo;    // 상품번호(FK)

	private String imgUrl;        // 이미지 URL
	private String orgfilename;   // 원본파일명
	private String filename;      // 서버저장파일명(UUID)

	private Integer sortNo;       // 정렬순서
	private String  isMain;       // 대표이미지(Y/N)

}