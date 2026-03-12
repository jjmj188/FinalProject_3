package com.spring.app.security.loginfail;

import java.io.IOException;

import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class MyAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        if (exception instanceof DisabledException && "IDLE_ACCOUNT".equals(exception.getMessage())) {
            // 휴면 계정 → 복구 팝업을 위해 이메일 세션 저장 후 idle=true 로 리다이렉트
            request.getSession().setAttribute("idleEmail", request.getParameter("email"));
            response.sendRedirect(request.getContextPath() + "/security/login?idle=true");
        } else {
            // 일반 로그인 실패 (이메일/비밀번호 불일치)
            response.sendRedirect(request.getContextPath() + "/security/login?error=true");
        }
    }
}
