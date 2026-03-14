package com.spring.app.mypage.domain;

import lombok.Data;

@Data
public class DeliveryAddressDTO {
    private int deliveryNo;
    private String memberEmail;
    private String label;
    private String receiverName;
    private String receiverPhone;
    private String postcode;
    private String address;
    private String detailAddress;
    private String extraAddress;
    private String isPrimary;
}
