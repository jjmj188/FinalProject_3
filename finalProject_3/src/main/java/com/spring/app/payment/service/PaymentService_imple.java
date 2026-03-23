package com.spring.app.payment.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.app.payment.domain.SettlementDTO;
import com.spring.app.payment.domain.TransactionDTO;
import com.spring.app.payment.model.PaymentDAO;
import com.spring.app.security.model.MemberDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService_imple implements PaymentService {

    private final PaymentDAO paymentDAO;
    private final MemberDAO  memberDAO;       // 캐시 잔액 업데이트용
    private final ObjectMapper objectMapper;

    @Value("${toss.payments.secret-key:test_sk_XXXXXXXXXXXXXXXXXXXXXXXX}")
    private String tossSecretKey;

    @Value("${toss.payments.api-url:https://api.tosspayments.com/v1}")
    private String tossApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    // ─────────────────────────────────────────────────────────────────────
    //  결제 수단 분류 상수
    // ─────────────────────────────────────────────────────────────────────

    /** 토스 에스크로 API로 정산이 처리되는 결제 수단 */
    private static final java.util.Set<String> TOSS_ESCROW_TYPES =
            java.util.Set.of("카드결제", "가상계좌", "간편결제");

    // ─────────────────────────────────────────────────────────────────────

    /**
     * 결제 준비 — TRANSACTIONS 테이블에 READY 상태로 INSERT
     */
    @Override
    @Transactional
    public TransactionDTO createTransaction(int productNo, String buyerEmail, String paymentType, int amount) {

        // 동일 상품+구매자의 READY 거래가 이미 있으면 재사용 (중복 방지)
        Map<String, Object> checkMap = new HashMap<>();
        checkMap.put("productNo", productNo);
        checkMap.put("buyerEmail", buyerEmail);
        TransactionDTO existing = paymentDAO.selectReadyTransaction(checkMap);
        if (existing != null) {
            if (existing.getTossOrderId() == null) {
                String newOrderId = "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
                existing.setTossOrderId(newOrderId);
            }
            return existing;
        }

        String orderId = "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        TransactionDTO dto = new TransactionDTO();
        dto.setProductNo(productNo);
        dto.setBuyerEmail(buyerEmail);
        dto.setPayStatus("READY");
        dto.setAmount(amount);

        // 무료나눔: 토스 결제 없이 처리 (DB 제약조건에 맞게 '캐시결제' + NULL toss keys)
        if ("무료나눔".equals(paymentType)) {
            dto.setPaymentType("캐시결제");
            dto.setTossOrderId(null);
            dto.setTossPayKey(null);
            dto.setUseEscrow("N");
        } else {
            dto.setPaymentType(paymentType);
            dto.setTossOrderId(orderId);
            dto.setTossPayKey("PENDING");
            dto.setUseEscrow("Y");
        }

        paymentDAO.insertTransaction(dto);

        if (dto.getTossOrderId() == null) {
            dto.setTossOrderId(orderId);
        }

        return dto;
    }

    /**
     * 무료나눔 거래 완료 처리
     */
    @Override
    @Transactional
    public void completeFreeOrder(int transactionId) {
        paymentDAO.updateFreeOrderComplete(transactionId);

        TransactionDTO txn = paymentDAO.selectTransactionById(transactionId);
        if (txn != null) {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("productNo", txn.getProductNo());
            productMap.put("tradeStatus", "예약중");
            paymentDAO.updateProductTradeStatus(productMap);
        }
    }

    /**
     * 토스 결제 승인 — /v1/payments/confirm API 호출
     */
    @Override
    @Transactional
    public Map<String, Object> confirmPayment(String paymentKey, String orderId, int amount, String requestIp) {
        Map<String, Object> result = new LinkedHashMap<>();

        TransactionDTO txn = paymentDAO.selectTransactionByOrderId(orderId);
        if (txn == null) {
            result.put("success", false);
            result.put("message", "거래 정보를 찾을 수 없습니다.");
            return result;
        }

        if (txn.getAmount() != amount) {
            result.put("success", false);
            result.put("message", "결제 금액이 일치하지 않습니다.");
            return result;
        }

        try {
            String authHeader = "Basic " +
                    Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("paymentKey", paymentKey);
            body.put("orderId", orderId);
            body.put("amount", amount);

            String bodyJson = objectMapper.writeValueAsString(body);

            savePaymentLog(txn.getTransactionId(), "REQUEST", tossApiUrl + "/payments/confirm",
                    "POST", bodyJson, null, null, null, null, requestIp);

            HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    tossApiUrl + "/payments/confirm",
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            String responseBody = response.getBody();
            int httpStatus = response.getStatusCode().value();

            savePaymentLog(txn.getTransactionId(), "RESPONSE", tossApiUrl + "/payments/confirm",
                    "POST", null, responseBody, httpStatus, null, null, requestIp);

            @SuppressWarnings("unchecked")
            Map<String, Object> tossResult = objectMapper.readValue(responseBody, Map.class);

            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("transactionId", txn.getTransactionId());
            updateMap.put("tossPayKey", paymentKey);
            updateMap.put("payStatus", "DONE");
            updateMap.put("approvedAt", tossResult.get("approvedAt"));
            paymentDAO.updateTransactionApproved(updateMap);

            Map<String, Object> productMap = new HashMap<>();
            productMap.put("productNo", txn.getProductNo());
            productMap.put("tradeStatus", "예약중");
            paymentDAO.updateProductTradeStatus(productMap);

            @SuppressWarnings("unchecked")
            Map<String, Object> cardInfo = (Map<String, Object>) tossResult.get("card");
            if (cardInfo != null) {
                Map<String, Object> cardMap = new HashMap<>();
                cardMap.put("transactionId", txn.getTransactionId());
                cardMap.put("cardCompanyCd", cardInfo.get("issuerCode"));
                cardMap.put("cardCompany", cardInfo.get("company"));
                cardMap.put("cardNum", cardInfo.get("number"));
                cardMap.put("cardType", cardInfo.get("cardType"));
                cardMap.put("installment", cardInfo.get("installmentPlanMonths"));
                cardMap.put("isNoInterest", Boolean.TRUE.equals(cardInfo.get("isInterestFree")) ? "Y" : "N");
                cardMap.put("ownerType", cardInfo.get("ownerType"));
                cardMap.put("acquireStatus", cardInfo.get("acquireStatus"));
                cardMap.put("receiptUrl", cardInfo.get("receiptUrl"));
                paymentDAO.insertCardPayment(cardMap);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> easyPayInfo = (Map<String, Object>) tossResult.get("easyPay");
            if (easyPayInfo != null) {
                Map<String, Object> easyMap = new HashMap<>();
                easyMap.put("transactionId", txn.getTransactionId());
                easyMap.put("provider", easyPayInfo.get("provider"));
                easyMap.put("discountAmt", easyPayInfo.get("discountAmount"));
                paymentDAO.insertEasyPayment(easyMap);
            }

            result.put("success", true);
            result.put("transactionId", txn.getTransactionId());
            result.put("paymentKey", paymentKey);
            result.put("orderId", orderId);

        } catch (HttpClientErrorException e) {
            log.error("토스 결제 승인 실패: {}", e.getResponseBodyAsString(), e);

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> errBody = objectMapper.readValue(e.getResponseBodyAsString(), Map.class);
                savePaymentLog(txn.getTransactionId(), "ERROR", tossApiUrl + "/payments/confirm",
                        "POST", null, e.getResponseBodyAsString(),
                        e.getStatusCode().value(),
                        (String) errBody.get("code"),
                        (String) errBody.get("message"),
                        requestIp);
            } catch (Exception ignore) {}

            Map<String, Object> abortMap = new HashMap<>();
            abortMap.put("transactionId", txn.getTransactionId());
            abortMap.put("payStatus", "ABORTED");
            paymentDAO.updateTradeStatus(abortMap);

            result.put("success", false);
            result.put("message", "결제 승인에 실패했습니다.");

        } catch (Exception e) {
            log.error("결제 승인 처리 중 오류", e);
            result.put("success", false);
            result.put("message", "결제 처리 중 오류가 발생했습니다.");
        }

        return result;
    }

    /**
     * 에스크로 구매확정 — 구매자가 상품 수령 후 구매확인.
     *
     * <p>결제 수단에 따라 정산 방식이 다르다.</p>
     * <ul>
     *   <li>카드결제 / 가상계좌 / 간편결제 → 토스 에스크로 구매확정 API 호출
     *       (토스가 보관 중인 결제금을 판매자에게 정산)</li>
     *   <li>캐시결제 → 판매자 CASH_BALANCE 직접 증가</li>
     *   <li>계좌이체 → SETTLEMENTS 테이블에 정산 대기 row 삽입
     *       (관리자가 수동 송금 후 '완료' 처리)</li>
     * </ul>
     */
    @Override
    @Transactional
    public Map<String, Object> confirmEscrow(int transactionId, String buyerEmail) {
        Map<String, Object> result = new LinkedHashMap<>();

        // ── 1. 거래 조회 및 기본 검증 ──────────────────────────────────────
        TransactionDTO txn = paymentDAO.selectTransactionById(transactionId);
        if (txn == null) {
            result.put("success", false);
            result.put("message", "거래 정보를 찾을 수 없습니다.");
            return result;
        }

        if (!buyerEmail.equals(txn.getBuyerEmail())) {
            result.put("success", false);
            result.put("message", "구매자만 구매확인이 가능합니다.");
            return result;
        }

        if (!"DONE".equals(txn.getPayStatus())) {
            result.put("success", false);
            result.put("message", "결제 완료 상태에서만 구매확인이 가능합니다.");
            return result;
        }

        // ── 2. TRANSACTIONS 거래완료 처리 (공통) ─────────────────────────────
        paymentDAO.updateEscrowConfirm(transactionId);

        // ── 3. 상품 상태 → '판매완료' (공통) ─────────────────────────────────
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("productNo", txn.getProductNo());
        productMap.put("tradeStatus", "판매완료");
        paymentDAO.updateProductTradeStatus(productMap);

        // ── 4. 결제 수단별 판매자 정산 처리 ─────────────────────────────────
        String paymentType = txn.getPaymentType();

        if (TOSS_ESCROW_TYPES.contains(paymentType)) {
            // ── 4-A. 토스 결제: 에스크로 구매확정 API 호출 ──────────────────
            boolean tossOk = callTossEscrowConfirm(txn, result);
            if (!tossOk) {
                // API 실패 시 트랜잭션 롤백 (거래완료/상품상태 변경도 함께 롤백됨)
                throw new RuntimeException("토스 에스크로 구매확정 API 호출 실패");
            }

        } else if ("캐시결제".equals(paymentType)) {
            // ── 4-B. 캐시결제: 판매자 CASH_BALANCE 직접 증가 ────────────────
            Map<String, Object> cashMap = new HashMap<>();
            cashMap.put("email",  txn.getSellerEmail());
            cashMap.put("amount", txn.getAmount());
            memberDAO.updateCashBalance(cashMap);
            log.info("[정산-캐시] transactionId={} seller={} amount={}원 캐시 지급 완료",
                    transactionId, txn.getSellerEmail(), txn.getAmount());

        } else if ("계좌이체".equals(paymentType)) {
            // ── 4-C. 계좌이체: SETTLEMENTS 테이블에 정산 대기 row 삽입 ───────
            Integer primaryAccountId = paymentDAO.selectPrimaryAccountId(txn.getSellerEmail());

            SettlementDTO settlement = new SettlementDTO();
            settlement.setTransactionId(transactionId);
            settlement.setSellerEmail(txn.getSellerEmail());
            settlement.setAccountId(primaryAccountId);   // 계좌 미등록 시 null
            settlement.setAmount(txn.getAmount());

            paymentDAO.insertSettlement(settlement);

            if (primaryAccountId == null) {
                log.warn("[정산-계좌이체] transactionId={} seller={} 대표 계좌 미등록 — 관리자 수동 확인 필요",
                        transactionId, txn.getSellerEmail());
            } else {
                log.info("[정산-계좌이체] transactionId={} seller={} amount={}원 정산 대기 등록 (accountId={})",
                        transactionId, txn.getSellerEmail(), txn.getAmount(), primaryAccountId);
            }

        } else {
            // 정의되지 않은 결제수단 — 로그만 남기고 정산 누락 경고
            log.error("[정산-미처리] transactionId={} paymentType={} — 정산 로직 없음, 수동 처리 필요",
                    transactionId, paymentType);
        }

        result.put("success", true);
        result.put("message", "구매확인이 완료되었습니다.");
        return result;
    }

    // ─────────────────────────────────────────────────────────────────────
    //  조회
    // ─────────────────────────────────────────────────────────────────────

    @Override
    public TransactionDTO getTransactionByOrderId(String orderId) {
        return paymentDAO.selectTransactionByOrderId(orderId);
    }

    @Override
    public TransactionDTO getTransactionById(int transactionId) {
        return paymentDAO.selectTransactionById(transactionId);
    }

    // ─────────────────────────────────────────────────────────────────────
    //  내부 헬퍼 메서드
    // ─────────────────────────────────────────────────────────────────────

    /**
     * 토스 에스크로 구매확정 API 호출.
     *
     * <pre>
     * POST /v1/payments/{paymentKey}/escrow/orders
     * Body: {"status": "CONFIRMED"}
     * </pre>
     *
     * 성공이면 true, 실패이면 false를 반환하고 result에 에러 메시지를 담는다.
     */
    private boolean callTossEscrowConfirm(TransactionDTO txn, Map<String, Object> result) {
        String paymentKey = txn.getTossPayKey();
        String url = tossApiUrl + "/payments/" + paymentKey + "/escrow/orders";

        try {
            String authHeader = "Basic " +
                    Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> body = new HashMap<>();
            body.put("status", "CONFIRMED");
            String bodyJson = objectMapper.writeValueAsString(body);

            savePaymentLog(txn.getTransactionId(), "REQUEST", url, "POST",
                    bodyJson, null, null, null, null, null);

            HttpEntity<String> entity = new HttpEntity<>(bodyJson, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

            int httpStatus = response.getStatusCode().value();
            savePaymentLog(txn.getTransactionId(), "RESPONSE", url, "POST",
                    null, response.getBody(), httpStatus, null, null, null);

            log.info("[정산-토스에스크로] transactionId={} paymentKey={} 구매확정 API 성공 (HTTP {})",
                    txn.getTransactionId(), paymentKey, httpStatus);
            return true;

        } catch (HttpClientErrorException e) {
            String errBody = e.getResponseBodyAsString();
            log.error("[정산-토스에스크로] transactionId={} 구매확정 API 실패: {}", txn.getTransactionId(), errBody, e);

            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> errMap = objectMapper.readValue(errBody, Map.class);
                savePaymentLog(txn.getTransactionId(), "ERROR", url, "POST",
                        null, errBody, e.getStatusCode().value(),
                        (String) errMap.get("code"), (String) errMap.get("message"), null);
            } catch (Exception ignore) {}

            result.put("success", false);
            result.put("message", "토스 에스크로 구매확정 중 오류가 발생했습니다.");
            return false;

        } catch (Exception e) {
            log.error("[정산-토스에스크로] transactionId={} 예기치 못한 오류", txn.getTransactionId(), e);
            result.put("success", false);
            result.put("message", "구매확정 처리 중 오류가 발생했습니다.");
            return false;
        }
    }

    /**
     * 결제 로그 저장 헬퍼
     */
    private void savePaymentLog(Integer transactionId, String logType, String apiEndpoint,
                                 String httpMethod, String requestData, String responseData,
                                 Integer httpStatus, String errorCode, String errorMsg, String requestIp) {
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put("transactionId", transactionId);
            logMap.put("logType", logType);
            logMap.put("apiEndpoint", apiEndpoint);
            logMap.put("httpMethod", httpMethod);
            logMap.put("requestData", requestData);
            logMap.put("responseData", responseData);
            logMap.put("httpStatus", httpStatus);
            logMap.put("errorCode", errorCode);
            logMap.put("errorMsg", errorMsg);
            logMap.put("requestIp", requestIp);
            paymentDAO.insertPaymentLog(logMap);
        } catch (Exception e) {
            log.warn("결제 로그 저장 실패", e);
        }
    }
}
