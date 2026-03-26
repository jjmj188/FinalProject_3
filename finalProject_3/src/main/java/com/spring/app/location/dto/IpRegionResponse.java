package com.spring.app.location.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IpRegionResponse {

    private boolean success;
    private String keyword;
    private String regionLabel;
    private String message;

    public static IpRegionResponse ok(String keyword, String regionLabel) {
        IpRegionResponse dto = new IpRegionResponse();
        dto.setSuccess(true);
        dto.setKeyword(keyword);
        dto.setRegionLabel(regionLabel);
        return dto;
    }

    public static IpRegionResponse fail(String message) {
        IpRegionResponse dto = new IpRegionResponse();
        dto.setSuccess(false);
        dto.setMessage(message);
        return dto;
    }
}