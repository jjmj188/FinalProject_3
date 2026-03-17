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
public class GoogleOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private MemberDAO memberDAO;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 구글 응답은 attributes가 flat 구조 (response 중첩 없음)
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String socialId = (String) attributes.get("sub");   // 구글 고유 ID
        String email    = (String) attributes.get("email");
        String name     = (String) attributes.get("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_not_found"), "구글 이메일 정보를 가져올 수 없습니다.");
        }

        // 기존 회원 조회 (STATUS 조건 없음)
        MemberDTO member = memberDAO.findByEmailForSocial(email);

        // 동일 이메일로 일반/다른 소셜 가입 계정이 있으면 로그인 거부
        if (member != null && !"GOOGLE".equals(member.getSocialType())) {
            throw new OAuth2AuthenticationException(
                new OAuth2Error("email_already_exists"), "이미 해당 이메일로 가입된 계정이 있습니다.");
        }

        if (member == null) {
            // 자동 회원가입
            member = new MemberDTO();
            member.setEmail(email);
            member.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            member.setUserName(name != null && !name.isBlank() ? name : "구글사용자");

            String base = (name != null && !name.isBlank()) ? name : "user";
            if (base.length() > 45) base = base.substring(0, 45);
            member.setNickname(generateUniqueNickname(base));

            member.setPostcode("00000");
            member.setAddress("소셜로그인");
            member.setDetailaddress("-");
            member.setSocialType("GOOGLE");
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
