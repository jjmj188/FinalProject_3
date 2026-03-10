package com.spring.app.security.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

// JwtToken 클래스는 Spring Security + JWT 기반 인증 구조에서
// 클라이언트에게 발급해 줄 토큰(Response DTO) 역할을 하는 클래스이다.
@Data
@AllArgsConstructor
@Builder
public class JwtToken {

    private String grantType;          // 토큰 인증 방식 (Bearer)
    private String accessToken;        // 실제 인증에 사용되는 JWT 문자열
    private Long   accessTokenExpiresIn; // accessToken 의 만료 시간 (Expiration Time)
    private String refreshToken;       // accessToken 만료 시 재발급을 위한 토큰
}
