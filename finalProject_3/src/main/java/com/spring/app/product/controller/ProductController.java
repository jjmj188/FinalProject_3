package com.spring.app.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.app.index.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/product")
public class ProductController {
	//판매하기
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









