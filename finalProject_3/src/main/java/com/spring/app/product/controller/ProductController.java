package com.spring.app.product.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.app.index.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/product")
public class ProductController {

	 @GetMapping("/sell")
	    public String sellPage() {
	        return "product/sell"; // templates/product/sell.html
	    }
	 
	 @GetMapping("/share")
	    public String share() {
	        return "product/share";     // templates/product/share.html
	    }

	    @GetMapping("/auction")
	    public String auction() {
	        return "product/auction";   // templates/product/auction.html
	    }
	 
	}









