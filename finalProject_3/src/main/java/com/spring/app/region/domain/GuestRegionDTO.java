package com.spring.app.region.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GuestRegionDTO {

    private Long guestRegionNo;      // 비회원지역번호(PK)
    private String guestKey;         // 세션/디바이스/쿠키키
    private Long regionNo;           // 지역번호(FK)

    private BigDecimal latitude;     // 위도
    private BigDecimal longitude;    // 경도
    private LocalDateTime updatedAt; // 갱신시각
}