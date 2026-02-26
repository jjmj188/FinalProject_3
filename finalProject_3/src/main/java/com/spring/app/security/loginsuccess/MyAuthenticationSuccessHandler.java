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

    // ★ 추가된 부분: 생성자를 통해 로그인 성공 시 기본 동작을 '강제'합니다.
    public MyAuthenticationSuccessHandler() {
        // 1. 로그인 성공 시 기본으로 이동할 URL을 메인 페이지("/")로 설정
        setDefaultTargetUrl("/");
        // 2. 이전에 가려고 했던 페이지(예: /notification) 캐시를 무시하고 무조건 위 URL로 이동
        setAlwaysUseDefaultTargetUrl(true); 
    }

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

        // ★ 변경된 부분: 기존의 조건문을 없애고, 무조건 설정해둔 DefaultTargetUrl("/")로 이동하게 합니다.
        // 이 메서드 내부에서 스프링 시큐리티가 알아서 불필요한 이전 페이지 캐시도 지워주고 "/"로 보내줍니다.
        super.onAuthenticationSuccess(request, response, authentication);
    }
}