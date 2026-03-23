package com.spring.app.payment.domain;

import java.sql.Timestamp;

import lombok.Data;

@Data
public class TransactionDTO {

    private Integer transactionId;
    private Integer productNo;
    private String sellerEmail;
    private String buyerEmail;
    private Integer accountId;

    private String paymentType;       // 카드결제, 가상계좌, 간편결제
    private Integer amount;
    private String tradeStatus;       // 거래중, 거래완료, 취소, 환불중, 환불완료
    private Timestamp tradeDate;
    private Timestamp completeDate;

    // 토스 관련
    private String tossPayKey;
    private String tossOrderId;
    private String payStatus;         // READY, IN_PROGRESS, DONE, CANCELED ...
    private Timestamp approvedAt;

    private String useEscrow;         // Y/N

    // 채팅방 키 (안전결제 시 RESERVED_ROOM_ID 업데이트 용)
    private String roomId;

    // 조인용 (상품 정보)
    private String productName;
    private Integer productPrice;
    private String imgUrl;
    private String sellerName;
    private String buyerName;
}
