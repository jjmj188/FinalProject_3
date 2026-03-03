package com.spring.app.product.model;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;

@Mapper
public interface ProductDAO {

	 //판매하기(상품등록)
    int insertProduct(ProductDTO productDto);

    int insertProductImage(ProductImageDTO imageDto);

    int insertShippingOption(ProductShippingOptionDTO optionDto);

    int insertMeetLocation(ProductMeetLocationDTO locationDto);
    
    //장터(상품목록)
    List<ProductDTO> selectProductListSimple();
    
}