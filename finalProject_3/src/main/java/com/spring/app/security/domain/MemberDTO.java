package com.spring.app.security.domain;

import org.springframework.web.multipart.MultipartFile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {
    private String email;           // 회원이메일 (PK)
    private Long userNo;            // 회원번호 (SEQ_USER_NO)
    private String password;        // 비밀번호
    private String userName;        // 회원명 (user_name)
    private String nickname;        // 닉네임
    private String phone;           // 휴대폰번호
    private String gender;          // 성별 (M, F)
    private String birthDate;       // 생년월일 (YYYYMMDD)
    
    private String postcode;        // 우편번호
    private String address;         // 기본주소
    private String detailaddress;   // 상세주소
    private String extraaddress;    // 참고항목
    
    private String profileImg;      // 프로필이미지 파일명
    
    // 폼에서 전송되는 프로필 이미지 파일 객체
    private MultipartFile attach; 
}
