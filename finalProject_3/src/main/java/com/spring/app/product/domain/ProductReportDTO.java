package com.spring.app.product.domain;

import java.util.Date;

import lombok.Data;

@Data
public class ProductReportDTO {//게시글 신고하기 DTO

    private Integer reportId;

    private String reporterEmail;
    private String targetEmail;
    private Integer typeId;

    private Integer productNum;
    private Integer reviewNum;

    private String roomId;
    private String nosqlMsgKey;

    private String reportDetail;
    private String reportStatus;
    private Date reportDate;
    private String reportImg;

    // 프론트에서 넘어오는 신고 유형 확인용
    private String reportMainCategory;
    private String reportSubCategory;
}