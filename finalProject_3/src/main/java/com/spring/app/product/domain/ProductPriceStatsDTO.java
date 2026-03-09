package com.spring.app.product.domain;

import lombok.Data;

@Data
public class ProductPriceStatsDTO {

    private Integer avgPrice;
    private Integer maxPrice;
    private Integer minPrice;

}