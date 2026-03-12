package com.spring.app.ad.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.spring.app.admin.domain.AdDTO;
import com.spring.app.admin.service.AdminService;
import com.spring.app.common.FileManager;
import com.spring.app.security.domain.MemberDTO;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/ad")
public class AdController {

    private final AdminService adminService;
    private final FileManager fileManager = new FileManager();

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 광고 신청 페이지
    @GetMapping
    public String adPage(Model model, Principal principal) {

        if (principal != null) {
            String loginId = principal.getName();
            MemberDTO loginUser = adminService.getMemberById(loginId);
            model.addAttribute("loginUser", loginUser);
        }

        return "ad";  // templates/ad.html
    }

    // 광고 등록
    @PostMapping("/register")
    @ResponseBody
    public Map<String, Object> registerAd(AdDTO adDto, Principal principal) {

        Map<String, Object> result = new HashMap<>();

        try {

            if (principal == null) {
                result.put("success", false);
                result.put("message", "로그인이 필요합니다.");
                return result;
            }

            String loginId = principal.getName();
            MemberDTO loginUser = adminService.getMemberById(loginId);

            adDto.setUserNo(loginUser.getUserNo());

            int n = adminService.registerAd(adDto);

            result.put("success", n > 0);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
        }

        return result;
    }
}