package com.spring.app.product.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.app.common.FileManager;
import com.spring.app.index.service.IndexService;
import com.spring.app.product.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {
	
	private final ProductService service;
	private final FileManager fileManager;
	
	@Value("${file.photoupload-dir}")
	   private String photouploadPath;
	
	// === 판매하기(insert) 폼페이지 요청 === //
	 @PreAuthorize("isAuthenticated()")
	 @GetMapping("/sell")
	    public String sellPage() {
	        return "product/sell"; 
	    }
	 
	 
	 
	 //나눔하기
	 @GetMapping("/share")
	    public String share() {
	        return "product/share";    
	    }
	 //경매하기
    @GetMapping("/auction")
	    public String auction() {
	        return "product/auction";   
	    }
    
    //장터
    @GetMapping("/product_list")
	    public String product_list() {
	        return "product/product_list";   
	    }
    
  //시세조회
    @GetMapping("/price_check")
	    public String price_check() {
	        return "product/price_check";   
	    }
    
    //상품상세
    @GetMapping("/product_detail")
	    public String product_detail() {
	        return "product/product_detail";   
	    }
    
	 
  //판매자정보
    @GetMapping("/product_user_profile")
	    public String product_user_profile() {
	        return "product/product_user_profile";   
	    }
    
    
	}









