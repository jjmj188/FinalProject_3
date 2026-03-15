package com.spring.app.admin.domain;


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

	   private Long adId;
	    private int userNo;

	    private String brandName;
	    private String managerName;
	    private String companyEmail;
	    private String phone;
	    private String content;

	    private LocalDate startDate;
	    private LocalDate endDate;

	    private Integer durationWeeks;
	    private Long amount;

	    private String filePath;
	    private MultipartFile attachment;

	    private String agreedYn;

	    private String status;
	    private String rejectedReason;

	    private LocalDateTime createdAt;
	    private LocalDateTime approvedAt;
}