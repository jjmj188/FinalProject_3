package com.spring.app.chat.domain;

import lombok.Data;

@Data
public class ReportDTO {
    // 프론트엔드에서 넘어오는 데이터
    private String roomId;             // 채팅방 번호
    private String reportMainCategory; // 대분류 (예: 비매너 행위)
    private String reportSubCategory;  // 소분류 (예: 노쇼(No-Show))
    private String reportContent;      // 상세 내용

    // 서버에서 세팅해서 DB에 넣을 데이터
    private String reporterEmail;      // 신고자 (현재 로그인한 사람)
    private String targetEmail;        // 피신고자 (채팅 상대방)
    private int typeId;                // REPORT_TYPES 테이블에서 찾아온 번호
    private Integer productNum;        // 채팅방에 연결된 상품 번호
    private String nosqlMsgKey;        // 제약조건 통과용 임의 키
    private String reportImg;          // 첨부 이미지 파일명 (file_reports/ 폴더 기준)
}