package com.spring.app.security.loginsuccess;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
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
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberDAO memberDAO;

    public OAuth2AuthenticationSuccessHandler() {
        setDefaultTargetUrl("/");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws ServletException, IOException {

        HttpSession session = request.getSession();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        // 세션 저장
        Session_MemberDTO sessionDto = new Session_MemberDTO();
        sessionDto.setEmail(userDetails.getUsername());
        sessionDto.setUserName(userDetails.getMemberName());
        sessionDto.setNickname(userDetails.getNickname());
        session.setAttribute("loginuser", sessionDto);

        // JWT 발급
        JwtToken jwtToken = jwtTokenProvider.generateToken(authentication);

        ResponseCookie accessCookie = ResponseCookie.from("accessToken", jwtToken.getAccessToken())
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(3600)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", accessCookie.toString());

        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", jwtToken.getRefreshToken())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .sameSite("Strict")
                .build();
        response.addHeader("Set-Cookie", refreshCookie.toString());

        memberDAO.saveRefreshToken(userDetails.getUsername(), jwtToken.getRefreshToken());
        memberDAO.updateLastLoginDate(userDetails.getUsername());

        super.onAuthenticationSuccess(request, response, authentication);
    }
}
