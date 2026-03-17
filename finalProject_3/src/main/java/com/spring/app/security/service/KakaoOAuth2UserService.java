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
public class KakaoOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private MemberDAO memberDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @SuppressWarnings("unchecked")
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 카카오 고유 ID (Long 타입)
        String socialId = String.valueOf(attributes.get("id"));

        // kakao_account 안에 email과 profile 존재
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile     = (Map<String, Object>) kakaoAccount.get("profile");

        String email    = (String) kakaoAccount.get("email");
        String nickname = (String) profile.get("nickname");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_not_found"), "카카오 이메일 정보를 가져올 수 없습니다.");
        }

        // 기존 회원 조회 (STATUS 조건 없음)
        MemberDTO member = memberDAO.findByEmailForSocial(email);

        // 동일 이메일로 일반/다른 소셜 가입 계정이 있으면 로그인 거부
        if (member != null && !"KAKAO".equals(member.getSocialType())) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_already_exists"), "이미 해당 이메일로 가입된 계정이 있습니다.");
        }

        if (member == null) {
            // 자동 회원가입
            member = new MemberDTO();
            member.setEmail(email);
            member.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            member.setUserName(nickname != null && !nickname.isBlank() ? nickname : "카카오사용자");

            String base = (nickname != null && !nickname.isBlank()) ? nickname : "user";
            if (base.length() > 45) base = base.substring(0, 45);
            member.setNickname(generateUniqueNickname(base));

            member.setPostcode("00000");
            member.setAddress("소셜로그인");
            member.setDetailaddress("-");
            member.setSocialType("KAKAO");
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
