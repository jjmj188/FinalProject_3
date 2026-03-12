package com.spring.app.payment.model;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.spring.app.payment.domain.TransactionDTO;

@Mapper
public interface PaymentDAO {

    // 거래 생성 (READY 상태)
    int insertTransaction(TransactionDTO dto);

    // 거래 조회 (tossOrderId 기준)
    TransactionDTO selectTransactionByOrderId(String tossOrderId);

    // 거래 조회 (transactionId 기준)
    TransactionDTO selectTransactionById(int transactionId);

    // 결제 승인 후 업데이트
    int updateTransactionApproved(Map<String, Object> paraMap);

    // 거래 상태 업데이트
    int updateTradeStatus(Map<String, Object> paraMap);

    // 에스크로 구매확인 (거래완료 처리)
    int updateEscrowConfirm(int transactionId);

    // 무료나눔 거래 완료
    int updateFreeOrderComplete(int transactionId);

    // 상품 거래상태 업데이트
    int updateProductTradeStatus(Map<String, Object> paraMap);

    // 결제 로그 저장
    int insertPaymentLog(Map<String, Object> paraMap);

    // 카드결제 상세 저장
    int insertCardPayment(Map<String, Object> paraMap);

    // 간편결제 상세 저장
    int insertEasyPayment(Map<String, Object> paraMap);
}
