package com.spring.app.payment.service;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
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
import com.spring.app.payment.domain.TransactionDTO;
import com.spring.app.payment.model.PaymentDAO;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService_imple implements PaymentService {

    private final PaymentDAO paymentDAO;
    private final ObjectMapper objectMapper;

    @Value("${toss.payments.secret-key:test_sk_XXXXXXXXXXXXXXXXXXXXXXXX}")
    private String tossSecretKey;

    @Value("${toss.payments.api-url:https://api.tosspayments.com/v1}")
    private String tossApiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 결제 준비 — TRANSACTIONS 테이블에 READY 상태로 INSERT
     */
    @Override
    @Transactional
    public TransactionDTO createTransaction(int productNo, String buyerEmail, String paymentType) {

        String orderId = "ORDER_" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);

        TransactionDTO dto = new TransactionDTO();
        dto.setProductNo(productNo);
        dto.setBuyerEmail(buyerEmail);
        dto.setPayStatus("READY");

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

        // 무료나눔이어도 orderId를 반환 (JS 리다이렉트용, DB에는 저장 안 됨)
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
        // 무료나눔 거래 완료 처리 (PAY_STATUS=DONE)
        paymentDAO.updateFreeOrderComplete(transactionId);

        // 상품 상태를 '예약중'으로 변경
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

        // 1) DB에서 거래 확인
        TransactionDTO txn = paymentDAO.selectTransactionByOrderId(orderId);
        if (txn == null) {
            result.put("success", false);
            result.put("message", "거래 정보를 찾을 수 없습니다.");
            return result;
        }

        // 금액 검증
        if (txn.getAmount() != amount) {
            result.put("success", false);
            result.put("message", "결제 금액이 일치하지 않습니다.");
            return result;
        }

        // 2) 토스 결제 승인 API 호출
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

            // 요청 로그 저장
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

            // 응답 로그 저장
            savePaymentLog(txn.getTransactionId(), "RESPONSE", tossApiUrl + "/payments/confirm",
                    "POST", null, responseBody, httpStatus, null, null, requestIp);

            @SuppressWarnings("unchecked")
            Map<String, Object> tossResult = objectMapper.readValue(responseBody, Map.class);

            // 3) 승인 성공 → DB 업데이트
            Map<String, Object> updateMap = new HashMap<>();
            updateMap.put("transactionId", txn.getTransactionId());
            updateMap.put("tossPayKey", paymentKey);
            updateMap.put("payStatus", "DONE");
            updateMap.put("approvedAt", tossResult.get("approvedAt"));

            paymentDAO.updateTransactionApproved(updateMap);

            // 상품 상태를 '예약중'으로 변경
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("productNo", txn.getProductNo());
            productMap.put("tradeStatus", "예약중");
            paymentDAO.updateProductTradeStatus(productMap);

            // 카드결제 상세 저장
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

            // 간편결제 상세 저장
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

            // 에러 로그 저장
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

            // 거래 상태를 ABORTED로
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
     * 에스크로 구매확인 — 구매자가 상품 수령 후 구매확인
     */
    @Override
    @Transactional
    public Map<String, Object> confirmEscrow(int transactionId, String buyerEmail) {
        Map<String, Object> result = new LinkedHashMap<>();

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

        // 거래완료 처리
        paymentDAO.updateEscrowConfirm(transactionId);

        // 상품 상태를 '판매완료'로 변경
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("productNo", txn.getProductNo());
        productMap.put("tradeStatus", "판매완료");
        paymentDAO.updateProductTradeStatus(productMap);

        result.put("success", true);
        result.put("message", "구매확인이 완료되었습니다.");
        return result;
    }

    @Override
    public TransactionDTO getTransactionByOrderId(String orderId) {
        return paymentDAO.selectTransactionByOrderId(orderId);
    }

    @Override
    public TransactionDTO getTransactionById(int transactionId) {
        return paymentDAO.selectTransactionById(transactionId);
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
