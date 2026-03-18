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
    private String payStatus;
    private String tradeDate;
    private String imgUrl;
    private Integer transactionId;
    private Integer hasReview;   // 0: 미작성, 1: 작성완료
    private String sellerEmail;
    private String carrierCode;
    private String invoiceNo;
}
