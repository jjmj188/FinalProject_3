package com.spring.app.security.jwt;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import com.spring.app.security.domain.CustomUserDetails;
import com.spring.app.security.domain.JwtToken;
import com.spring.app.security.domain.MemberDTO;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/*
    Spring Security 와 JSON Web Token 을 사용하여
    인증(Authentication) 및 인가(Authorization)를 처리하기 위해
    JWT의 생성, 서명 검증, Claim 추출, Authentication 객체 변환 기능을 제공하는 클래스이다.
*/
@Component
@Slf4j
public class JwtTokenProvider {

    private static final String AUTHORITIES_KEY = "auth";

    private static final String BEARER_TYPE = "Bearer";

    private static final long ACCESS_TOKEN_EXPIRE_TIME  = 1000L * 60 * 2;            // 2분 (테스트용)
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7; // 7일

    private final SecretKey secretKey;

    // 생성자
    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }


    /* 인증된 사용자의 정보(Authentication)를 가지고
       AccessToken 과 RefreshToken 을 생성하여 JWT 토큰을 생성해주는 메서드 */
    public JwtToken generateToken(Authentication authentication) {

        // 권한 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // AccessToken 유효기간 (현재로부터 설정된 시간 후)
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        System.out.println("====== [JWT 생성] ======");
        System.out.println("### email       : " + authentication.getName());
        System.out.println("### auth claim  : " + authorities);
        System.out.println("### 발급 시각   : " + new Date(now));
        System.out.println("### 만료 시각   : " + accessTokenExpiresIn + "  (" + (ACCESS_TOKEN_EXPIRE_TIME / 1000) + "초 후)");
        System.out.println("### 남은 시간   : " + (ACCESS_TOKEN_EXPIRE_TIME / 1000) + "초");
        System.out.println("=======================");

        // AccessToken 생성
        String accessToken = Jwts.builder()
                .subject(authentication.getName())       // 인증된 사용자의 이메일
                .claim(AUTHORITIES_KEY, authorities)     // 권한 정보를 Claim에 포함
                .expiration(accessTokenExpiresIn)        // 유효기간
                .signWith(secretKey)                     // 서명
                .compact();

        // RefreshToken 생성
        String refreshToken = Jwts.builder()
                .expiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(secretKey)
                .compact();

        return JwtToken.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }


    /* accessToken 을 복호화하여 인증 정보(Authentication)를 가져오는 메서드 */
    public Authentication getAuthentication(String accessToken) {

        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        System.out.println("====== [JWT 검증 - 권한 복원] ======");
        System.out.println("### email       : " + claims.getSubject());
        System.out.println("### authorities : " + authorities);
        System.out.println("### ADMIN 여부  : " + authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        System.out.println("===================================");

        // MemberDTO 생성 후 권한 목록 세팅 (jwt_jpa_board 방식)
        MemberDTO memberDTO = new MemberDTO();
        memberDTO.setEmail(claims.getSubject());
        memberDTO.setStatus(0);
        memberDTO.setAuthorities(
            authorities.stream()
                       .map(GrantedAuthority::getAuthority)
                       .collect(Collectors.toList())
        );

        // CustomUserDetails 생성 (단일 생성자 — jwt_jpa_board 방식)
        CustomUserDetails principal = new CustomUserDetails(memberDTO);

        return new UsernamePasswordAuthenticationToken(
                principal,
                "",
                principal.getAuthorities()
        );
    }


    /* 토큰 정보를 검증하는 메서드 */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token : ", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token : ", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token : ", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty : ", e);
        }
        return false;
    }


    /* accessToken 에서 Claims(클레임) 추출 — 만료된 토큰에서도 클레임 반환 (재발급 시 활용) */
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(accessToken)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            return e.getClaims(); // 만료되어도 클레임 반환
        }
    }


    /* 토큰에서 이메일(subject) 추출 */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }


    /* email + 권한 목록으로 직접 토큰 생성 (accessToken 이 아예 없을 때 재발급 폴백용) */
    public JwtToken generateTokenByEmail(String email, java.util.List<String> authorityList) {
        String authorities = String.join(",", authorityList);

        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);

        String accessToken = Jwts.builder()
                .subject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .expiration(accessTokenExpiresIn)
                .signWith(secretKey)
                .compact();

        String refreshToken = Jwts.builder()
                .expiration(new Date(now + REFRESH_TOKEN_EXPIRE_TIME))
                .signWith(secretKey)
                .compact();

        return JwtToken.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime())
                .refreshToken(refreshToken)
                .build();
    }
}
