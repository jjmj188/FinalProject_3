package com.spring.app.security.domain;

import lombok.Data;

@Data
public class Session_MemberDTO {
    // 세션에는 꼭 필요한 정보만 담습니다.
    private String email;      // 로그인 아이디 (이메일)
    private String userName;   // 회원명
    private String nickname;   // 닉네임
}