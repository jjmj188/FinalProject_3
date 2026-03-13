package com.spring.app.mypage.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.spring.app.security.domain.MemberDTO;
import com.spring.app.security.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/mypage")
public class MyPageController {

    @Autowired
    private MemberService memberService;

    @GetMapping("/main")
    public String myPageMain(Model model, Principal principal, HttpServletRequest request) {
        if (principal != null) {
            MemberDTO member = memberService.getMemberByEmail(principal.getName());
            if (member != null) {
                model.addAttribute("member", member);

                String ctxPath = request.getContextPath();
                String profileImg = member.getProfileImg();
                String profileImgUrl;
                if (profileImg == null || profileImg.isEmpty()) {
                    profileImgUrl = ctxPath + "/images/default_profile.png";
                } else if (profileImg.startsWith("http")) {
                    profileImgUrl = profileImg;
                } else {
                    profileImgUrl = ctxPath + "/resources/profile_images/" + profileImg;
                }
                model.addAttribute("profileImgUrl", profileImgUrl);
            }
        }
        return "mypage/mypage_main";
    }
}
