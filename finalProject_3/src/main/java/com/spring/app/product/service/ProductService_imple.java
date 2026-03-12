package com.spring.app.product.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.product.domain.ProductPriceStatsDTO;
import com.spring.app.product.domain.ProductPriceTrendDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
import com.spring.app.product.domain.SearchKeywordDTO;
import com.spring.app.product.domain.SearchLogDTO;
import com.spring.app.product.domain.WishlistDTO;
import com.spring.app.product.model.ProductDAO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService_imple implements ProductService {

    private final ProductDAO pdao;

    
    //판매하기 등록 
    
    @Override
    @Transactional
    public int productSellRegister(ProductDTO productDto,
                                   List<ProductImageDTO> imageDtoList,
                                   List<ProductShippingOptionDTO> shippingOptionList,
                                   List<ProductMeetLocationDTO> meetLocationList) {

        if ("나눔".equals(productDto.getSaleType())) {
            productDto.setProductPrice(0);
        }

        if ("판매".equals(productDto.getSaleType()) && productDto.getProductPrice() == null) {
            throw new RuntimeException("판매 상품은 가격이 필수입니다.");
        }

        // 1) PRODUCTS 저장
        int n = pdao.insertProduct(productDto);
        if (n != 1) {
            throw new RuntimeException("PRODUCTS insert fail");
        }

        Integer productNo = productDto.getProductNo();
        if (productNo == null) {
            throw new RuntimeException("productNo is null after insertProduct");
        }

        // 2) PRODUCT_IMAGE 저장
        if (imageDtoList != null && !imageDtoList.isEmpty()) {
            for (ProductImageDTO img : imageDtoList) {
                img.setProductNo(productNo);
                int m = pdao.insertProductImage(img);
                if (m != 1) {
                    throw new RuntimeException("PRODUCT_IMAGE insert fail");
                }
            }
        }

        // 3) PRODUCT_SHIPPING_OPTION 저장 (택배일 때)
        if ("택배".equals(productDto.getTradeMethod())) {
            if (shippingOptionList == null || shippingOptionList.isEmpty()) {
                throw new RuntimeException("shippingOptionList empty for ship trade");
            }

            for (ProductShippingOptionDTO opt : shippingOptionList) {
                opt.setProductNo(productNo);
                int m = pdao.insertShippingOption(opt);
                if (m != 1) {
                    throw new RuntimeException("PRODUCT_SHIPPING_OPTION insert fail");
                }
            }
        }

        // 4) PRODUCT_MEET_LOCATION 저장 (직거래일 때)
        if ("직거래".equals(productDto.getTradeMethod())) {
            if (meetLocationList == null || meetLocationList.isEmpty()) {
                throw new RuntimeException("meetLocationList empty for meet trade");
            }

            int sort = 1;
            for (ProductMeetLocationDTO loc : meetLocationList) {
                loc.setProductNo(productNo);
                if (loc.getSortNo() == null) loc.setSortNo(sort++);
                int m = pdao.insertMeetLocation(loc);
                if (m != 1) {
                    throw new RuntimeException("PRODUCT_MEET_LOCATION insert fail");
                }
            }
        }

        return 1;
    }


    // 장터(상품목록) 

    @Override
    public List<ProductDTO> selectProductListSimple() {
        return pdao.selectProductListSimple();
    }

    
   // 상품상세페이지 (전체 조회: 기본 + 이미지 + 옵션 + 위치)
    @Override
    public ProductDTO getProductDetailFull(Map<String, Object> paraMap) {
        ProductDTO productDto = pdao.selectProductDetail(paraMap);

        if (productDto == null) {
            return null;
        }

        int productNo = productDto.getProductNo();

        List<ProductImageDTO> imageList = pdao.selectProductImages(productNo);
        List<ProductShippingOptionDTO> shippingOptionList = pdao.selectShippingOption(productNo);
        List<ProductMeetLocationDTO> meetLocationList = pdao.selectMeetLocation(productNo);

        productDto.setImageList(imageList);
        productDto.setShippingOptionList(shippingOptionList);
        productDto.setMeetLocationList(meetLocationList);

        return productDto;
    }
    
    // 비슷한 거래물품
    @Override
    public List<ProductDTO> selectSimilarProducts(ProductDTO productDto) {

        List<ProductDTO> similarProductList = new ArrayList<>();

        if (productDto == null) {
            return similarProductList;
        }

        String productName = productDto.getProductName();
        String keyword1 = "";
        String keyword2 = "";
        String keyword3 = "";

        if (productName != null && !"".equals(productName.trim())) {

            String cleanedName = productName.replaceAll("\\[|\\]|\\(|\\)|,", " ").trim();
            String[] wordArr = cleanedName.split("\\s+");

            List<String> keywordList = new ArrayList<>();

            for (String word : wordArr) {

                if (word == null || "".equals(word.trim())) {
                    continue;
                }

                String trimWord = word.trim();

                if ("급처".equals(trimWord) ||
                    "판매".equals(trimWord) ||
                    "정리".equals(trimWord) ||
                    "새상품".equals(trimWord) ||
                    "상태좋음".equals(trimWord) ||
                    "상태최상".equals(trimWord)) {
                    continue;
                }

                keywordList.add(trimWord);

                if (keywordList.size() == 3) {
                    break;
                }
            }

            if (keywordList.size() > 0) {
                keyword1 = keywordList.get(0);
            }
            if (keywordList.size() > 1) {
                keyword2 = keywordList.get(1);
            }
            if (keywordList.size() > 2) {
                keyword3 = keywordList.get(2);
            }
        }

        Map<String, Object> paraMap = new LinkedHashMap<String, Object>();
        paraMap.put("productNo", productDto.getProductNo());
        paraMap.put("categoryNo", productDto.getCategoryNo());
        paraMap.put("productPrice", productDto.getProductPrice());
        paraMap.put("keyword1", keyword1);
        paraMap.put("keyword2", keyword2);
        paraMap.put("keyword3", keyword3);

        similarProductList = pdao.selectSimilarProducts(paraMap);

        if (similarProductList == null) {
            similarProductList = new ArrayList<>();
        }

        if (similarProductList.size() < 12) {

            List<Integer> excludeProductNoList = new ArrayList<>();
            excludeProductNoList.add(productDto.getProductNo());

            for (ProductDTO dto : similarProductList) {
                excludeProductNoList.add(dto.getProductNo());
            }

            paraMap.put("excludeProductNoList", excludeProductNoList);

            List<ProductDTO> fallbackList = pdao.selectSimilarProductsFallback(paraMap);

            if (fallbackList != null && !fallbackList.isEmpty()) {
                for (ProductDTO dto : fallbackList) {
                    similarProductList.add(dto);

                    if (similarProductList.size() == 12) {
                        break;
                    }
                }
            }
        }

        return similarProductList;
    }
    
    //검색
    @Override
    public List<String> wordSearchShow(Map<String, String> paraMap) {
        return pdao.wordSearchShow(paraMap);
    }
    
  //지역+상품검색+상품필터
    @Override
    public List<ProductDTO> selectProductListByCondition(String searchWord,
                                                         String areaDong,
                                                         String tradeAvailable,
                                                         String parcelAvailable,
                                                         Integer categoryNo,
                                                         String sortType,
                                                         Integer priceMin,
                                                         Integer priceMax) {
        return pdao.selectProductListByCondition(
                searchWord,
                areaDong,
                tradeAvailable,
                parcelAvailable,
                categoryNo,
                sortType,
                priceMin,
                priceMax
        );
    }
    
  
    
    //인기검색어
    @Override
    public int insertSearchLog(SearchLogDTO searchLogDto) {
        return pdao.insertSearchLog(searchLogDto);
    }
    
    @Override
    public List<SearchKeywordDTO> selectPopularKeywordList() {
        return pdao.selectPopularKeywordList();
    }

    //조회수
    @Override
    public void updateViewCount(int productNo) {
        pdao.updateViewCount(productNo);
    }

    // 최근 등록 상품 가격 통계
    @Override
    public ProductPriceStatsDTO selectRecentProductPriceStats(Map<String, Object> paraMap) {
        return pdao.selectRecentProductPriceStats(paraMap);
    }
    
    //상품더보기
    @Override
    public List<ProductDTO> selectProductListByConditionMore(Map<String, Object> paraMap) {
        return pdao.selectProductListByConditionMore(paraMap);
    }
  
    
    //찜
    @Override
    public boolean toggleWishlist(WishlistDTO wishlistDto) {

        int n = pdao.selectWishlistExists(wishlistDto);

        if(n > 0) {
            pdao.deleteWishlist(wishlistDto);
            return false; // 찜 취소됨
        }
        else {
            pdao.insertWishlist(wishlistDto);
            return true; // 찜됨
        }
    }

    @Override
    public boolean isWished(WishlistDTO wishlistDto) {
        return pdao.selectWishlistExists(wishlistDto) > 0;
    }
    
    //시세조회
    @Override
    public ProductPriceStatsDTO selectPriceCheckStats(Map<String, Object> paraMap) {
        return pdao.selectPriceCheckStats(paraMap);
    }

    @Override
    public List<ProductPriceTrendDTO> selectPriceCheckChartData(Map<String, Object> paraMap) {
        return pdao.selectPriceCheckChartData(paraMap);
    }

    @Override
    public List<ProductDTO> selectPriceCheckProductList(Map<String, Object> paraMap) {
        return pdao.selectPriceCheckProductList(paraMap);
    }

	//판매자정보
    @Override
    public ProductDTO selectSellerProfileByProductNo(int productNo) {
        return pdao.selectSellerProfileByProductNo(productNo);
    }

    @Override
    public List<ProductDTO> selectSellerProductsByProductNo(Map<String, Object> paraMap) {
        return pdao.selectSellerProductsByProductNo(paraMap);
    }

    @Override
    public int selectSellerProductCountByProductNo(int productNo) {
        return pdao.selectSellerProductCountByProductNo(productNo);
    }
    
    
}