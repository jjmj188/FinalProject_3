package com.spring.app.payment.controller;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.payment.domain.TransactionDTO;
import com.spring.app.payment.service.PaymentService;
import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.service.ProductService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequiredArgsConstructor
@RequestMapping("/payment/")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
    private final ProductService productService;

    @Value("${toss.payments.client-key:test_ck_XXXXXXXXXXXXXXXXXXXXXXXX}")
    private String tossClientKey;

    /**
     * 로그인 이메일 추출 헬퍼
     */
    private String getLoginEmail(Authentication authentication) {
        if (authentication == null) return null;
        String email = authentication.getName();
        if (email == null || email.trim().isEmpty() || "anonymousUser".equals(email)) return null;
        return email.trim();
    }

    /**
     * 안전결제 체크아웃 페이지
     */
    @GetMapping("checkout")
    public String checkout(@RequestParam("productNo") int productNo,
                           Authentication authentication,
                           Model model) {

        String buyerEmail = getLoginEmail(authentication);
        if (buyerEmail == null) {
            return "redirect:/security/login";
        }

        // 상품 상세 조회 (Map 파라미터 사용)
        Map<String, Object> paraMap = new HashMap<>();
        paraMap.put("productNo", productNo);
        paraMap.put("memberEmail", buyerEmail);

        ProductDTO product = productService.getProductDetailFull(paraMap);
        if (product == null) {
            return "redirect:/product/product_list";
        }

        // 본인 상품은 구매 불가
        if (buyerEmail.equals(product.getSellerEmail())) {
            model.addAttribute("errorMessage", "본인의 상품은 구매할 수 없습니다.");
            return "redirect:/product/product_detail/" + productNo;
        }

        model.addAttribute("product", product);
        model.addAttribute("tossClientKey", tossClientKey);
        model.addAttribute("buyerEmail", buyerEmail);

        return "payment/checkout";
    }

    /**
     * 결제 준비 API — 거래 생성 후 orderId 반환
     */
    @PostMapping("prepare")
    @ResponseBody
    public Map<String, Object> prepare(@RequestBody Map<String, Object> params,
                                        Authentication authentication) {

        Map<String, Object> result = new LinkedHashMap<>();

        String buyerEmail = getLoginEmail(authentication);
        if (buyerEmail == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        int productNo = ((Number) params.get("productNo")).intValue();
        String paymentType = (String) params.getOrDefault("paymentType", "카드결제");

        try {
            TransactionDTO txn = paymentService.createTransaction(productNo, buyerEmail, paymentType);

            // 생성된 거래의 금액 조회
            TransactionDTO fullTxn = paymentService.getTransactionByOrderId(txn.getTossOrderId());

            result.put("success", true);
            result.put("orderId", txn.getTossOrderId());
            result.put("transactionId", txn.getTransactionId());
            result.put("amount", fullTxn != null ? fullTxn.getAmount() : 0);

        } catch (Exception e) {
            log.error("결제 준비 실패", e);
            result.put("success", false);
            result.put("message", "결제 준비에 실패했습니다: " + e.getMessage());
        }

        return result;
    }

    /**
     * 토스 결제 성공 콜백 — 결제 승인 처리
     */
    @GetMapping("success")
    public String paymentSuccess(@RequestParam("paymentKey") String paymentKey,
                                  @RequestParam("orderId") String orderId,
                                  @RequestParam("amount") int amount,
                                  Authentication authentication,
                                  HttpServletRequest request,
                                  Model model) {

        String buyerEmail = getLoginEmail(authentication);
        if (buyerEmail == null) {
            return "redirect:/security/login";
        }

        String requestIp = request.getRemoteAddr();
        Map<String, Object> confirmResult = paymentService.confirmPayment(paymentKey, orderId, amount, requestIp);

        if (Boolean.TRUE.equals(confirmResult.get("success"))) {
            TransactionDTO txn = paymentService.getTransactionByOrderId(orderId);
            model.addAttribute("transaction", txn);
            model.addAttribute("paymentKey", paymentKey);
            return "payment/success";
        } else {
            model.addAttribute("errorCode", "CONFIRM_FAILED");
            model.addAttribute("errorMessage", confirmResult.get("message"));
            return "payment/fail";
        }
    }

    /**
     * 토스 결제 실패 콜백
     */
    @GetMapping("fail")
    public String paymentFail(@RequestParam(value = "code", required = false) String code,
                               @RequestParam(value = "message", required = false) String message,
                               @RequestParam(value = "orderId", required = false) String orderId,
                               Model model) {

        model.addAttribute("errorCode", code);
        model.addAttribute("errorMessage", message);
        model.addAttribute("orderId", orderId);

        return "payment/fail";
    }

    /**
     * 에스크로 구매확인 API
     */
    @PostMapping("escrow/confirm")
    @ResponseBody
    public Map<String, Object> escrowConfirm(@RequestBody Map<String, Object> params,
                                              Authentication authentication) {

        Map<String, Object> result = new LinkedHashMap<>();

        String buyerEmail = getLoginEmail(authentication);
        if (buyerEmail == null) {
            result.put("success", false);
            result.put("message", "로그인이 필요합니다.");
            return result;
        }

        int transactionId = ((Number) params.get("transactionId")).intValue();
        return paymentService.confirmEscrow(transactionId, buyerEmail);
    }
}