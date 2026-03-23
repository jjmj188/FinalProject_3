package com.spring.app.payment.service;

import java.util.Map;

import com.spring.app.payment.domain.TransactionDTO;

public interface PaymentService {

    // 결제 준비 (거래 생성, orderId 생성)
    TransactionDTO createTransaction(int productNo, String buyerEmail, String paymentType, int amount, String roomId);

    // 토스 결제 승인
    Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount, String requestIp);

    // 에스크로 구매확인 (구매자가 수령 확인)
    Map<String, Object> confirmEscrow(int transactionId, String buyerEmail);

    // 무료나눔 거래 완료
    void completeFreeOrder(int transactionId);

    // 거래 조회
    TransactionDTO getTransactionByOrderId(String orderId);

    TransactionDTO getTransactionById(int transactionId);
}
