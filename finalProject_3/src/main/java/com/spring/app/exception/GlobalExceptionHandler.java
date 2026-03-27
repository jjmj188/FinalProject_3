package com.spring.app.exception;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public Object handleGeneralException(Exception e,
                                         HttpServletRequest request,
                                         Model model) {
        log.error("[GlobalExceptionHandler] Unhandled exception at {}: {}", request.getRequestURI(), e.getMessage(), e);

        String xrw = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(xrw);
        String accept = request.getHeader("Accept");
        boolean wantsJson = accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
        String contentType = request.getContentType();
        boolean isJsonRequest = contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);

        if (isAjax || wantsJson || isJsonRequest) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", "서버 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
        }
        model.addAttribute("errorMessage", "서버 오류가 발생했습니다.");
        return "error";
    }

    @ExceptionHandler(BadWordException.class)
    public Object handleBadWordException(BadWordException e,
                                         HttpServletRequest request,
                                         Model model) {

        // 1) AJAX 여부 판단
        String xrw = request.getHeader("X-Requested-With");
        boolean isAjax = "XMLHttpRequest".equalsIgnoreCase(xrw);

        // 2) Accept 헤더에 JSON 포함 여부 판단
        String accept = request.getHeader("Accept");
        boolean wantsJson = accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);

        // 3) Content-Type 이 JSON 인지(요청 바디 기준)
        String contentType = request.getContentType();
        boolean isJsonRequest = contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE);

        // ===== JSON으로 내려줘야 하는 경우 =====
        // (AJAX이거나, Accept가 JSON이거나, JSON 요청이면 JSON 응답)
        if (isAjax || wantsJson || isJsonRequest) {
            Map<String, Object> body = new HashMap<>();
            body.put("success", false);
            body.put("message", e.getMessage());

            // 금지어는 "서버 에러"가 아니라 "요청값 문제"라 400이 맞음
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
        }

        // ===== 화면(HTML)로 내려줘야 하는 경우 =====
        model.addAttribute("errorMessage", e.getMessage());
        return "error_forbidden_word"; // templates/error_forbidden_word.html
    }

   
}