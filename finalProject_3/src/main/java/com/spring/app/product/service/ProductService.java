package com.spring.app.product.service;

import java.util.List;
import java.util.Map;

import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;

public interface ProductService {

	//판매하기 등록
    int productSellRegister(ProductDTO productDto,
                            List<ProductImageDTO> imageDtoList,
                            List<ProductShippingOptionDTO> shippingOptionList,
                            List<ProductMeetLocationDTO> meetLocationList);

    //장터(상품목록)
	List<ProductDTO> selectProductListSimple();

	//상품상세페이지
	ProductDTO getProductDetailFull(int productNo);
	
	//검색
	List<String> wordSearchShow(Map<String, String> paraMap);

	//검색된 상품목록 보이기
	List<ProductDTO> searchProductList(String searchWord);

	
	}
	

	
	
