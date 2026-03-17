package com.spring.app.security.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    @Autowired
    private NaverOAuth2UserService naverOAuth2UserService;

    @Autowired
    private GoogleOAuth2UserService googleOAuth2UserService;

    @Autowired
    private KakaoOAuth2UserService kakaoOAuth2UserService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        if ("naver".equals(registrationId)) {
            return naverOAuth2UserService.loadUser(userRequest);
        } else if ("google".equals(registrationId)) {
            return googleOAuth2UserService.loadUser(userRequest);
        } else if ("kakao".equals(registrationId)) {
            return kakaoOAuth2UserService.loadUser(userRequest);
        }

        throw new OAuth2AuthenticationException(
            new OAuth2Error("unsupported_provider"), "지원하지 않는 소셜 로그인입니다: " + registrationId);
    }
}
