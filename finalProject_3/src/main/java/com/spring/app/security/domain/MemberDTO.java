package com.spring.app.security.domain;

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
}
