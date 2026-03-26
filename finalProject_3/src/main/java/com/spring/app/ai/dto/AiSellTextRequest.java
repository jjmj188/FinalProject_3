package com.spring.app.ai.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class AiSellTextRequest {

    private String productName;
    private String categoryName;
    private String productPrice;
    private String productDesc;
}