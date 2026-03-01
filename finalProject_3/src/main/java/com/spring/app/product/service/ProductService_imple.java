package com.spring.app.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spring.app.product.domain.ProductDTO;
import com.spring.app.product.domain.ProductImageDTO;
import com.spring.app.product.domain.ProductMeetLocationDTO;
import com.spring.app.product.domain.ProductShippingOptionDTO;
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

        // (선택) 판매인데 price가 null이면 막기
        if ("판매".equals(productDto.getSaleType()) && productDto.getProductPrice() == null) {
            throw new RuntimeException("판매 상품은 가격이 필수입니다.");
        }
    	
        // 1) PRODUCTS 저장 (selectKey로 productNo 세팅)
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

    //장터(상품목록)
    @Override
    public List<ProductDTO> selectProductListSimple() {
        return pdao.selectProductListSimple();
    }
}