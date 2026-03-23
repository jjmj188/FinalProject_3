package com.spring.app.payment.model;

import java.util.Map;

import org.apache.ibatis.annotations.Mapper;

import com.spring.app.payment.domain.SettlementDTO;
import com.spring.app.payment.domain.TransactionDTO;

@Mapper
public interface PaymentDAO {

    // 거래 생성 (READY 상태)
    int insertTransaction(TransactionDTO dto);

    // 동일 상품+구매자의 기존 READY 거래 조회 (중복 방지)
    TransactionDTO selectReadyTransaction(Map<String, Object> paraMap);

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

    // ─────────────────────────────────────────────────────────────────────
    //  정산 관련 (구매확정 후 판매자 대금 처리)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 캐시결제 구매확정: 판매자 CASH_BALANCE 증가
     *
     * @param paraMap {"email": sellerEmail, "amount": txn.getAmount()}
     */
    int updateSellerCashBalance(Map<String, Object> paraMap);

    /**
     * 계좌이체 구매확정: SETTLEMENTS 테이블에 정산 대기 row 삽입
     *
     * @param dto SettlementDTO (transactionId, sellerEmail, accountId, amount 필수)
     */
    int insertSettlement(SettlementDTO dto);

    /**
     * 판매자의 대표 정산계좌 ID 조회 (없으면 null)
     *
     * @param sellerEmail 판매자 이메일
     * @return ACCOUNT_ID or null
     */
    Integer selectPrimaryAccountId(String sellerEmail);
}
