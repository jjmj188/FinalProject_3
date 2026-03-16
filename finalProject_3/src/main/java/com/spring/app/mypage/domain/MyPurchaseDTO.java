package com.spring.app.mypage.domain;

import lombok.Data;

@Data
public class MyPurchaseDTO {
    private Integer productNo;
    private String productName;
    private Integer productPrice;
    private String saleType;
    private Integer amount;
    private String paymentType;
    private String tradeStatus;
    private String tradeDate;
    private String imgUrl;
}
