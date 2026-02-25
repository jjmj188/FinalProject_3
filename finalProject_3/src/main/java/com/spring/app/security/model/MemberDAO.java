package com.spring.app.security.model;

import com.spring.app.security.domain.MemberDTO;

public interface MemberDAO {
    
    // 이메일 중복 확인
    int checkEmail(String email);

    // 닉네임 중복 확인
    int checkNickname(String nickname);

    // 휴대폰 번호 중복 확인
    int checkPhone(String phone);

    // 회원가입
    int insertMember(MemberDTO member);
    
    // ★ [추가] 로그인 시 이메일로 사용자 정보(DTO) 가져오기
    MemberDTO findByEmail(String email);
}