package com.spring.app.security.service;

import com.spring.app.security.domain.MemberDTO;

public interface MemberService {
    boolean isEmailExist(String email);
    boolean isNicknameExist(String nickname);
    boolean isPhoneExist(String phone);
    void registerMember(MemberDTO member);
    String sendSms(String phone);
    String findEmailByPhone(String phone);

    // 비밀번호 재설정 (휴대폰 번호 기준)
    void updatePasswordByPhone(String phone, String encodedPassword);
}