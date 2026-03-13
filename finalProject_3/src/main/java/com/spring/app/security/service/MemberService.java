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

    // 휴면 계정 여부 확인 (전화번호 기준)
    boolean isIdlePhone(String phone);

    // 휴면 해제 (IDLE=0, USER_DORMANT 삭제)
    void reactivateMember(String email);

    // 이메일로 회원 정보 조회
    MemberDTO getMemberByEmail(String email);

    // 프로필 수정 (닉네임 + 프로필 이미지)
    void updateProfile(MemberDTO member);
}