package com.spring.app.region.domain;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class RegionDTO {

    private Long regionNo;          // 지역번호(PK)
    private String regionName;      // 지역명
    private Long parentRegionNo;    // 상위지역번호(FK, 자기참조)
    private BigDecimal latitude;    // 위도
    private BigDecimal longitude;   // 경도
    private String regionType;      // 지역유형
}