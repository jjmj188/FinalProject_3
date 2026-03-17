package com.spring.app.security.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.spring.app.security.domain.CustomUserDetails;
import com.spring.app.security.domain.MemberDTO;
import com.spring.app.security.model.MemberDAO;

@Service
public class NaverOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private MemberDAO memberDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 네이버 응답은 "response" 키 안에 실제 사용자 정보가 존재
        Map<String, Object> response = (Map<String, Object>) oAuth2User.getAttributes().get("response");

        String socialId = (String) response.get("id");
        String email    = (String) response.get("email");
        String name     = (String) response.get("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_not_found"), "네이버 이메일 정보를 가져올 수 없습니다.");
        }

        // 기존 회원 조회 (STATUS 조건 없이 — 탈퇴/휴면 계정도 포함해서 중복 INSERT 방지)
        MemberDTO member = memberDAO.findByEmailForSocial(email);

        // 동일 이메일로 일반 가입된 계정이 있으면 로그인 거부
        if (member != null && !"NAVER".equals(member.getSocialType())) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_already_exists"),
                "이미 해당 이메일로 가입된 계정이 있습니다.");
        }

        if (member == null) {
            // 자동 회원가입 처리
            member = new MemberDTO();
            member.setEmail(email);
            member.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            member.setUserName(name != null && !name.isBlank() ? name : "네이버사용자");

            String base = (name != null && !name.isBlank()) ? name : "user";
            if (base.length() > 45) base = base.substring(0, 45);
            member.setNickname(generateUniqueNickname(base));

            member.setPostcode("00000");
            member.setAddress("소셜로그인");
            member.setDetailaddress("-");
            member.setSocialType("NAVER");
            member.setSocialId(socialId);

            memberDAO.insertSocialMember(member);
            memberDAO.insertAuthority(email);

            member = memberDAO.findByEmailForSocial(email);
        }

        List<String> authorities = memberDAO.findAuthoritiesByEmail(email);
        if (authorities == null || authorities.isEmpty()) {
            authorities = List.of("ROLE_USER");
        }
        member.setAuthorities(authorities);

        return new CustomUserDetails(member);
    }

    private String generateUniqueNickname(String base) {
        String candidate = base;
        int i = 2;
        while (memberDAO.checkNickname(candidate) > 0) {
            candidate = base + "_" + i;
            i++;
        }
        return candidate;
    }
}
