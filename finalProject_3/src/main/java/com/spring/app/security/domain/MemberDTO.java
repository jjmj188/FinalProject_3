package com.spring.app.security.domain;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MemberDTO {
    private String email;
    private int userNo;      // DB의 USER_NO (숫자형)
    private String password;
    private String userName;
    private String nickname;
    private String phone;
    private String gender;
    private String birthDate;
    
    private String postcode;      // 우편번호
    private String address;       // 주소
    private String detailaddress; // 상세주소
    private String extraaddress;  // 참고항목
    
    private String regDate;
    private int status;
    private int idle;
    private int suspended;
    private long cashBalance;
    private double mannerTemp;
    private String profileImg;
    private MultipartFile attach;
    private String defaultProfile; // 일러스트 선택 시 dicebear API URL

    // 소셜 로그인
    private String socialType; // 'NAVER' 등
    private String socialId;   // 네이버 고유 ID

    // 권한 목록 (jwt_jpa_board 방식: AUTHORITIES 테이블에서 로드)
    private List<String> authorities;
}
