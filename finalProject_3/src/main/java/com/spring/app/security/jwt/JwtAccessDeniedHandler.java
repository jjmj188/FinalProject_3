package com.spring.app.security.jwt;

import java.io.IOException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/*
    인증은 됐지만 권한(Authorization)이 없는 사용자가 접근할 때
    호출되는 핸들러.
    HTTP 403 Forbidden 응답을 반환한다.
*/
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {
        // 권한 없음 → 403 Forbidden
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }
}
