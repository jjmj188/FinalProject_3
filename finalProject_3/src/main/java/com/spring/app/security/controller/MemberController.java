package com.spring.app.security.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.security.domain.MemberDTO;

@Controller
@RequestMapping("/member")
public class MemberController {
	
	@GetMapping("/login")
    public String loginForm() {
        return "member/login"; // templates/member/login.html 매핑
    }

    // 회원가입 페이지 이동
    @GetMapping("/register")
    public String registerForm() {
        return "member/register"; 
    }
    

    // 이메일 중복 확인 (AJAX)
    @ResponseBody
    @PostMapping("/emailDuplicateCheck")
    public Map<String, Boolean> emailDuplicateCheck(@RequestParam String email) {
        Map<String, Boolean> map = new HashMap<>();
        
        boolean isExist = false; // 임시: DB에 존재하면 true
        
        map.put("isExist", isExist);
        return map;
    }
    
    // 닉네임 중복 확인 (AJAX) - DB에 UNIQUE 조건이 있으므로 추가
    @ResponseBody
    @PostMapping("/nicknameDuplicateCheck")
    public Map<String, Boolean> nicknameDuplicateCheck(@RequestParam String nickname) {
        Map<String, Boolean> map = new HashMap<>();
        
        // TODO: Service -> DAO 거쳐서 실제 DB 조회 로직 구현
        boolean isExist = false; 
        
        map.put("isExist", isExist);
        return map;
    }

    // 회원가입 처리
    @PostMapping("/registerEnd")
    public String registerEnd(MemberDTO memberDTO) {
        
        // 1. 생년월일 형식 변환 (YYYY-MM-DD -> YYYYMMDD) DB 구조 맞춤
        if(memberDTO.getBirthDate() != null) {
            memberDTO.setBirthDate(memberDTO.getBirthDate().replaceAll("-", ""));
        }
        
        
        return "redirect:/"; // 메인 페이지로 이동
    }
}
