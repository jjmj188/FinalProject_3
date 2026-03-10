package com.spring.app.security.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.spring.app.security.domain.JwtToken;
import com.spring.app.security.jwt.JwtTokenProvider;
import com.spring.app.security.model.MemberDAO;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/*
    AccessToken 만료 시 RefreshToken 으로 새 토큰을 재발급하는 컨트롤러.
    jwt_jpa_board 의 AuthController.reissue() 패턴을 따른다.
*/
@RestController
@Slf4j
public class TokenController {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberDAO memberDAO;

    /**
     * RefreshToken으로 AccessToken + RefreshToken 재발급
     * POST /security/reissue
     *
     * 동작 흐름 (jwt_jpa_board AuthController 참고):
     *  1. 쿠키에서 accessToken(만료), refreshToken 추출
     *  2. refreshToken 유효성 검증
     *  3. 만료된 accessToken 에서 parseClaims 로 Authentication(권한 포함) 복원
     *  4. DB 저장 refreshToken 과 비교
     *  5. 새 토큰 발급 → 쿠키 + DB 업데이트
     */
    @PostMapping("/security/reissue")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 accessToken, refreshToken 추출
        String accessToken  = null;
        String refreshToken = null;

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("accessToken".equals(c.getName()))  accessToken  = c.getValue();
                if ("refreshToken".equals(c.getName())) refreshToken = c.getValue();
            }
        }

        // refreshToken 검증
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            log.info("유효하지 않은 RefreshToken");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않은 RefreshToken입니다.");
        }

        // 만료된 accessToken 에서 Authentication 복원 (권한 포함)
        // parseClaims() 가 ExpiredJwtException 발생 시에도 Claims 를 반환하므로 가능
        Authentication authentication = null;
        if (accessToken != null) {
            try {
                authentication = jwtTokenProvider.getAuthentication(accessToken);
            } catch (Exception e) {
                log.info("AccessToken 에서 Authentication 복원 실패: {}", e.getMessage());
            }
        }

        // accessToken 도 없으면 refreshToken 으로 email 만 추출
        String email = (authentication != null)
                ? authentication.getName()
                : jwtTokenProvider.getEmailFromToken(refreshToken);

        // DB 저장 refreshToken 과 비교
        String savedToken = memberDAO.findRefreshTokenByEmail(email);
        if (savedToken == null || !refreshToken.equals(savedToken)) {
            log.info("RefreshToken 불일치 - email: {}", email);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("RefreshToken이 일치하지 않습니다.");
        }

        // 새 토큰 발급 (복원된 Authentication 으로 기존 권한 그대로 유지)
        JwtToken newToken = (authentication != null)
                ? jwtTokenProvider.generateToken(authentication)
                : jwtTokenProvider.generateTokenByEmail(email, memberDAO.findAuthoritiesByEmail(email));

        // DB RefreshToken 업데이트
        memberDAO.saveRefreshToken(email, newToken.getRefreshToken());

        // 새 쿠키 설정
        ResponseCookie accessCookie = ResponseCookie.from("accessToken", newToken.getAccessToken())
                .httpOnly(true).secure(false).path("/").maxAge(3600).sameSite("Strict").build();
        ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", newToken.getRefreshToken())
                .httpOnly(true).secure(false).path("/").maxAge(60 * 60 * 24 * 7).sameSite("Strict").build();

        response.addHeader("Set-Cookie", accessCookie.toString());
        response.addHeader("Set-Cookie", refreshCookie.toString());

        log.info("토큰 재발급 완료 - email: {}", email);
        return ResponseEntity.ok(Map.of("accessTokenExpiresIn", newToken.getAccessTokenExpiresIn()));
    }
}
