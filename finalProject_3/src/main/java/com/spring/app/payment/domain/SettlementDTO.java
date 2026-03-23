package com.spring.app.payment.domain;

import java.sql.Timestamp;

import lombok.Data;

/**
 * 계좌이체 결제의 정산 대기 정보를 담는 DTO.
 * 구매확정 시 INSERT되며, 관리자가 수동 송금 처리 후 STATUS를 '완료'로 변경한다.
 */
@Data
public class SettlementDTO {

    private Integer settlementId;
    private Integer transactionId;
    private String  sellerEmail;
    private Integer accountId;       // 판매자 대표 정산계좌 ID (없으면 null)

    private Integer amount;
    private String  status;          // 대기 / 완료 / 실패
    private Timestamp createdAt;
    private Timestamp completedAt;
    private String  memo;

    // 조인용 (관리자 화면 조회 시)
    private String  sellerName;
    private String  bankName;
    private String  accountNum;
    private String  accountHolder;
}
