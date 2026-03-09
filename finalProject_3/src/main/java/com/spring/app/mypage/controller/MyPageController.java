package com.spring.app.mypage.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mypage")
public class MyPageController {

    @GetMapping("/main")
    public String myPageMain() {
        // 아까 만든 mypage_main.html 파일의 경로를 리턴
        return "mypage/mypage_main"; 
    }
}
