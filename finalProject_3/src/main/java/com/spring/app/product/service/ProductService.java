package com.spring.app.product.service;

import java.util.List;
import java.util.Map;

import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
import com.spring.app.product.domain.SearchKeywordDTO;
import com.spring.app.product.domain.SearchLogDTO;

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

	

	//지역+상품검색+상품필터
	List<ProductDTO> selectProductListByCondition(String searchWord,
            String areaDong,
            String tradeAvailable,
            String parcelAvailable,
            Integer categoryNo,
            String sortType,
            Integer priceMin,
            Integer priceMax);

	//인기검색어
	void insertSearchLog(SearchLogDTO searchLogDto);
	List<SearchKeywordDTO> selectPopularKeywordList();
	
	//조회수
	void updateViewCount(int productNo);
	
	
	
	
	}
	
	
	
	
