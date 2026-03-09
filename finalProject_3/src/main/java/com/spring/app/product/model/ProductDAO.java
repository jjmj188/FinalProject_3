package com.spring.app.product.model;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.product.domain.ProductPriceStatsDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
import com.spring.app.product.domain.SearchKeywordDTO;
import com.spring.app.product.domain.SearchLogDTO;
import com.spring.app.product.domain.WishlistDTO;

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

	//지역+상품검색+상품필터
	List<ProductDTO> selectProductListByCondition(@Param("searchWord") String searchWord,
            @Param("areaDong") String areaDong,
            @Param("tradeAvailable") String tradeAvailable,
            @Param("parcelAvailable") String parcelAvailable,
            @Param("categoryNo") Integer categoryNo,
            @Param("sortType") String sortType,
            @Param("priceMin") Integer priceMin,
            @Param("priceMax") Integer priceMax);
	
	//인기검색어
	void insertSearchLog(SearchLogDTO searchLogDto);
	List<SearchKeywordDTO> selectPopularKeywordList();
	
	//조회수
	int updateViewCount(int productNo);
	
	// 최근 등록 상품 가격 통계
	ProductPriceStatsDTO selectRecentProductPriceStats(Map<String, Object> paraMap);
	
	//상품 더보기
	List<ProductDTO> selectProductListByConditionMore(Map<String, Object> paraMap);
	
	//찜
	int insertWishlist(WishlistDTO wishlistDto);
	int deleteWishlist(WishlistDTO wishlistDto);
	int selectWishlistExists(WishlistDTO wishlistDto);
    
}