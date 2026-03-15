package com.spring.app.admin.domain;

import lombok.Data;
import java.util.List;

@Data
public class ProductDetailDTO {
    private int productNo;
    private String productName;
    private String productDesc;
    private long productPrice;
    private String sellerEmail;
    private String sellerNickname;
    private String categoryName;
    private String tradeStatus;
    private String tradeMethod;
    private String regDate;
    private List<String> images;
}
