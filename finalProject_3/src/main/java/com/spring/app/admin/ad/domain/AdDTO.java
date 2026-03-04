package com.spring.app.admin.ad.domain;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class AdDTO {

    // 1. 기본 광고 정보
    private Long adId;            // AD_ID (PK)
    private String brandName;
    private String managerName;   // MANAGER_NAME
    private String companyEmail;  // COMPANY_EMAIL
    private String phone;         // PHONE
    private String content;       // CONTENT (CLOB은 String으로 매핑 가능)
    
    // 2. 날짜 및 기간 정보
    // HTML date 타입과 매핑하기 위해 String으로 받거나 LocalDate로 선언
    private String startDate;     // START_DATE (YYYY-MM-DD)
    private String endDate;       // END_DATE (YYYY-MM-DD)
    private Integer durationWeeks; // DURATION_WEEKS (1~4)
    private Long amount;          // AMOUNT (Long 사용 권장)

    // 3. 파일 및 약관 정보
    private String filePath;      // FILE_PATH (DB 저장 경로)
    private MultipartFile attachment; // HTML에서 실제 업로드된 파일 객체
    private String agreedYn;      // AGREED_YN ('Y'/'N')

    // 4. 관리 상태 정보
    private String status;         // STATUS (WAIT/CONFIRM/REJECT)
    private String rejectedReason; // REJECTED_REASON
    
    // 5. 시스템 날짜 정보
    private LocalDateTime createdAt;  // CREATED_AT (신청일)
    private LocalDateTime approvedAt; // APPROVED_AT (처리일)
}