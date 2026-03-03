package com.spring.app.product.domain;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class ProductDTO {

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

    // 폼 바인딩 받을 리스트(서버에서 JSON 파싱 후 세팅)
    private List<ProductShippingOptionDTO> shippingOptionList;
    private List<ProductMeetLocationDTO> meetLocationList;

    // ✅ hidden input으로 넘어오는 JSON 문자열(프론트에서 name=shippingOptionsJson / meetLocationsJson)
    private String shippingOptionsJson;
    private String meetLocationsJson;
    
 // 목록 표시용 지역 (강남구 등)
    private String areaGu;
    private String imgUrl;
    
}