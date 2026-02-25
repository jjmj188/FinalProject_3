package com.spring.app.security.loginsuccess;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.spring.app.security.domain.CustomUserDetails;
import com.spring.app.security.domain.Session_MemberDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class MyAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {
        
        HttpSession session = request.getSession();

        // 인증된 사용자 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 세션에 담을 DTO 세팅
        Session_MemberDTO sessionDto = new Session_MemberDTO();
        sessionDto.setEmail(userDetails.getUsername());
        sessionDto.setUserName(userDetails.getMemberName());
        sessionDto.setNickname(userDetails.getNickname());

        // 세션에 "loginuser" 라는 이름으로 저장
        session.setAttribute("loginuser", sessionDto);

        // 로그인 전 URL이 있었다면 그곳으로 이동 (없으면 기본 메인페이지)
        String redirectUrl = (String) session.getAttribute("prevURLPage");
        
        if (redirectUrl != null) {
            session.removeAttribute("prevURLPage");
            super.getRedirectStrategy().sendRedirect(request, response, redirectUrl);
        } else {
            // 기본 URL (메인 페이지 등)로 이동
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }
}