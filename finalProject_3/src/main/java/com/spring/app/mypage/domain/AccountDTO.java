package com.spring.app.mypage.domain;

import lombok.Data;

@Data
public class AccountDTO {
    private int accountId;
    private String email;
    private String bankName;
    private String accountNum;
    private String accountHolder;
    private String isPrimary;
}
