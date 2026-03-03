package com.spring.app.product.domain;

import lombok.Data;

@Data
public class ProductMeetLocationDTO {

    private Integer locationNo;     // 위치번호(PK)
    private Integer productNo;      // 상품번호(FK)

    private String placeName;       // 장소명 (예: OO역 3번출구)
    private String fullAddress;     // 전체주소
    private Double latitude;        // 위도 (NUMBER(10,7))
    private Double longitude;       // 경도 (NUMBER(10,7))

    private Integer sortNo;         // 정렬순서
}