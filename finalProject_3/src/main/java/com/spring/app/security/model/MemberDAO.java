package com.spring.app.security.model;

import java.util.List;
import java.util.Map;

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
    
    String findEmailByPhone(String phone);
    void updatePasswordByPhone(Map<String, String> paramMap);

    // AUTHORITIES 테이블에서 권한 목록 조회
    List<String> findAuthoritiesByEmail(String email);

    // RefreshToken CRUD
    void saveRefreshToken(@org.apache.ibatis.annotations.Param("email") String email,
                          @org.apache.ibatis.annotations.Param("rtValue") String rtValue);
    String findRefreshTokenByEmail(String email);
    void deleteRefreshToken(String email);
}