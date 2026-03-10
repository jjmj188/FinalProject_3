package com.spring.app.security.loginsuccess;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.spring.app.security.domain.CustomUserDetails;
import com.spring.app.security.domain.JwtToken;
import com.spring.app.security.domain.Session_MemberDTO;
import com.spring.app.security.jwt.JwtTokenProvider;
import com.spring.app.security.model.MemberDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class MyAuthenticationSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberDAO memberDAO;

    public MyAuthenticationSuccessHandler() {
        setDefaultTargetUrl("/");
        setAlwaysUseDefaultTargetUrl(true);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        HttpSession session = request.getSession();

        // 인증된 사용자 정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 세션에 담을 DTO 세팅 (기존 유지)
        Session_MemberDTO sessionDto = new Session_MemberDTO();
        sessionDto.setEmail(userDetails.getUsername());
        sessionDto.setUserName(userDetails.getMemberName());
        sessionDto.setNickname(userDetails.getNickname());

        // 세션에 "loginuser" 라는 이름으로 저장 (기존 유지)
        session.setAttribute("loginuser", sessionDto);

        System.out.println("====== [권한 확인] 로그인 성공 ======");
        System.out.println("### 이메일      : " + userDetails.getUsername());
        System.out.println("### 보유 권한   : " + userDetails.getAuthorities());
        System.out.println("### ADMIN 여부  : " + userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        System.out.println("=====================================");

        // JWT 토큰 생성
        System.out.println("~~~~~~~~~~~ JWT 토큰 생성 시작");
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        System.out.println("######## jwtToken : " + jwtToken);
        System.out.println("### accessToken  = " + jwtToken.getAccessToken());
        System.out.println("### refreshToken = " + jwtToken.getRefreshToken());
        System.out.println("### expiresIn    = " + jwtToken.getAccessTokenExpiresIn());

        // AccessToken → HttpOnly 쿠키
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtToken.getAccessToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(3600)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());

        // RefreshToken → HttpOnly 쿠키 + DB 저장
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwtToken.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());
        memberDAO.saveRefreshToken(userDetails.getUsername(), jwtToken.getRefreshToken());

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
