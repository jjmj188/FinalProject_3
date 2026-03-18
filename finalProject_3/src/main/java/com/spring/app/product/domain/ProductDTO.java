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

    private String carrierCode;  // 택배사 코드
    private String invoiceNo;    // 송장번호

    private Integer viewCount;
    private Date regDate;

    private String categoryName;
    
    /* ============================= */
    /* 판매자 정보 */
    /* ============================= */
    private String sellerName;
    private Double mannerTemp;
    private String sellerProfileImg;

    /* ============================= */
    /* 배송 옵션 리스트 */
    /* ============================= */
    private List<ProductShippingOptionDTO> shippingOptionList;

    /* ============================= */
    /* 거래 위치 리스트 */
    /* ============================= */
    private List<ProductMeetLocationDTO> meetLocationList;

    /* ============================= */
    /* 이미지 리스트 */
    /* ============================= */
    private List<ProductImageDTO> imageList;

    /* ============================= */
    /* JSON 데이터 */
    /* ============================= */
    private String shippingOptionsJson;
    private String meetLocationsJson;

    /* ============================= */
    /* 상품 목록 표시용 데이터 */
    /* ============================= */
    private String placeName;   // 목록에서 장소명 표시
    private String imgUrl;      // 대표 이미지
    
    private int canAccessDetail;
    
    private String areaGu;
    /* ============================= */
    /* 찜 */
    /* ============================= */
    private Integer wishCount;
    private boolean wished;
    /* ============================= */
    /* 판매자 정보 */
    /* ============================= */
    private Integer reviewCount;
    private Integer safePayCount;
    private Integer tradeCount;
    
    // 관리자 페이지 전용 의심 플래그
    private boolean suspect;
    
    //로그인 접속 시간
    private Date lastLoginDate;
    
    //리뷰
    private List<ReviewDTO> recentReviewList;
   
}