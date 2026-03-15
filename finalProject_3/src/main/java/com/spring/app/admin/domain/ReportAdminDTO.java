package com.spring.app.admin.domain;

import lombok.Data;
import java.util.Date;

@Data
public class ReportAdminDTO {
    private long reportId;
    private String reporterEmail;
    private String targetEmail;
    private String typeName;
    private String mainCategory;
    private String reportDetail;
    private Date reportDate;
    private String reportStatus;
}
