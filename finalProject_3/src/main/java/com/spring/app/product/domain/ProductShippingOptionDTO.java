package com.spring.app.product.domain;

import lombok.Data;

@Data
public class ProductShippingOptionDTO {

	private Integer optionNo;     // 옵션번호(PK)
	private Integer productNo;    // 상품번호(FK)

	private String  parcelType;   // 배송타입(일반택배, CU반값, GS반값, 무료배송)
	private Integer shippingFee;  // 배송비

}