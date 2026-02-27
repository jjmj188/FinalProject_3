package com.spring.app.index.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.spring.app.index.service.IndexService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class IndexController {

    private final IndexService service;

    @GetMapping("/")
    public String main() {
        return "index";
    }
}








