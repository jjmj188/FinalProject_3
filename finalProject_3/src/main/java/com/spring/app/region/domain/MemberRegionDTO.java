package com.spring.app.region.domain;

import lombok.Data;

@Data
public class MemberRegionDTO {

    private Long memberRegionNo;    // 회원지역번호(PK)
    private String memberEmail;     // 회원이메일(FK)
    private Long regionNo;          // 지역번호(FK)

    private String isActive;        // 현재활성지역여부(Y/N)
    private String isVerified;      // 동네인증여부(Y/N)
}