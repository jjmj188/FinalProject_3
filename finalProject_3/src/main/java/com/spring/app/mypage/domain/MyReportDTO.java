package com.spring.app.mypage.domain;

import lombok.Data;

@Data
public class MyReportDTO {
    private Integer reportId;
    private String reportDetail;
    private String reportStatus;
    private String reportDate;
    private String typeName;
    private String mainCategory;
    private String productName;
    private Integer productNo;
    private String imgUrl;
    private String reporterNickname;
    private String targetNickname;
}
