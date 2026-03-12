package com.spring.app.mypage.domain;

import lombok.Data;

@Data
public class NotificationDTO {
    private int notiNo;
    private int userNo;
    private String notiType;
    private String title;
    private String message;
    private String regDate;
    private int readStatus;
}
