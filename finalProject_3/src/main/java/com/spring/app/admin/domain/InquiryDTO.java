
package com.spring.app.admin.domain;

import lombok.Data;
import java.util.Date;

@Data // Getter, Setter, ToString 등을 자동으로 생성해줍니다 (Lombok 사용 시)
public class InquiryDTO {

    private int inquiryId;          // INQUIRY_ID (PK)
    private String memberEmail;     // MEMBER_EMAIL (FK)
    private String title;           // TITLE
    private String content;         // CONTENT (CLOB)
    private Date createdAt;         // CREATED_AT
    
    // 답변 관련 필드
    private String inquiryStatus;   // INQUIRY_STATUS (대기, 답변완료)
    private String adminAnswer;     // ADMIN_ANSWER (CLOB)
    private Date answeredAt;        // ANSWERED_AT

    // 새로 추가한 필드들
    private String isFaq;          // IS_FAQ ('Y', 'N')
    private String isPrivate;      // IS_PRIVATE ('Y', 'N')
    
    // (선택사항) 작성자 닉네임을 화면에 보여주고 싶다면 추가
    private String nickname;        // MEMBER 테이블과 JOIN해서 가져올 때 사용
}