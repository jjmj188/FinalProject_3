package com.spring.app.product.model;

import java.util.List;
import java.util.Map;

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
    
    //상품상세(기본정보+이미지+배송온셥+거래위치)
    ProductDTO selectProductDetail(int productNo);

    List<ProductImageDTO> selectProductImages(int productNo);

    List<ProductShippingOptionDTO> selectShippingOption(int productNo);

    List<ProductMeetLocationDTO> selectMeetLocation(int productNo);

    //검색
	List<String> wordSearchShow(Map<String, String> paraMap);

	//검색된 상품목록 보이기
	List<ProductDTO> searchProductList(String searchWord);
    
}