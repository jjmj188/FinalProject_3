package com.spring.app.index.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.spring.app.index.service.ProductService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final ProductService service;

    @GetMapping("/")
    public String main() {
        return "index";
    }
}








