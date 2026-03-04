package com.spring.app.product.domain;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ProductDTO {

    /* ============================= */
    /* 상품 기본 정보 (PRODUCTS) */
    /* ============================= */
    private Integer productNo;
    private String sellerEmail;
    private Integer categoryNo;

    private String saleType;
    private String productName;
    private Integer productPrice;
    private String productDesc;

    private String productCondition;
    private String tradeStatus;
    private String tradeMethod;

    private Integer viewCount;
    private Date regDate;

    private String categoryName; //카테고리 이름

    /* ============================= */
    /* 배송 옵션 리스트 (PRODUCT_SHIPPING_OPTION) */
    /* ============================= */
    // 상품 등록 및 상세 조회 시 사용
    private List<ProductShippingOptionDTO> shippingOptionList;


    /* ============================= */
    /* 거래 위치 리스트 (PRODUCT_MEET_LOCATION) */
    /* ============================= */
    // 상품 등록 및 상세 조회 시 사용
    private List<ProductMeetLocationDTO> meetLocationList;


    /* ============================= */
    /* 이미지 리스트 (PRODUCT_IMAGE) */
    /* ============================= */
    // ⭐ 상품 상세 페이지에서 여러 이미지 표시용
    private List<ProductImageDTO> imageList;


    /* ============================= */
    /* JSON 데이터 (폼 데이터 전달용) */
    /* ============================= */
    // JS에서 옵션 데이터를 JSON으로 보낼 때 사용
    private String shippingOptionsJson;
    private String meetLocationsJson;


    /* ============================= */
    /* 상품 목록 표시용 데이터 */
    /* ============================= */
    // 목록 페이지에서 지역 표시 (강남구 등)
    private String areaGu;

    // 목록 페이지 대표 이미지
    private String imgUrl;

}