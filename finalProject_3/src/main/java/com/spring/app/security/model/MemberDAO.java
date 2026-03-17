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

    // 마지막 로그인 일자 갱신
    void updateLastLoginDate(String email);

    // 휴면 처리 배치
    void moveMembersToUserDormant();
    void setIdleForDormantMembers();

    // 휴면 계정 여부 확인 (암호화된 전화번호)
    int checkIdlePhone(String phone);

    // 휴면 해제
    void reactivateMember(String email);
    void deleteFromUserDormant(String email);

    // 프로필 수정 (닉네임 + 프로필 이미지)
    void updateProfile(MemberDTO member);

    // 회원 탈퇴 (STATUS = 0)
    void withdrawMember(String email);

    // RefreshToken CRUD
    void saveRefreshToken(@org.apache.ibatis.annotations.Param("email") String email,
                          @org.apache.ibatis.annotations.Param("rtValue") String rtValue);
    String findRefreshTokenByEmail(String email);
    void deleteRefreshToken(String email);

    // 소셜 로그인 전용 회원 조회 (STATUS 조건 없음)
    MemberDTO findByEmailForSocial(String email);

    // 소셜 로그인 자동 회원가입
    int insertSocialMember(MemberDTO member);
    void insertAuthority(String email);
}