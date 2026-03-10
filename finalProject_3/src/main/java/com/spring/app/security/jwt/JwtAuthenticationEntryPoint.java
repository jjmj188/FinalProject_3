package com.spring.app.security.jwt;

import java.io.IOException;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
    인증(Authentication)이 되지 않은 사용자가 인증이 필요한 요청을 보낼 때
    호출되는 핸들러.
    HTTP 401 Unauthorized 응답을 반환한다.
*/
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        // 인증 실패 → 401 Unauthorized
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    }
}
