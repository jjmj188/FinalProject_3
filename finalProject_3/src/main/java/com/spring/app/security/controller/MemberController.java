package com.spring.app.security.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.spring.app.security.domain.MemberDTO;
import com.spring.app.security.service.MemberService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/member")
public class MemberController {
	
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginForm() {
        return "member/login"; // templates/member/login.html
    }

    @GetMapping("/register")
    public String registerForm() {
        return "member/register";
    }
    
    // 이메일 중복 확인
    @ResponseBody
    @PostMapping("/emailDuplicateCheck")
    public Map<String, Boolean> emailDuplicateCheck(@RequestParam("email") String email) {
        Map<String, Boolean> map = new HashMap<>();
        boolean isExist = memberService.isEmailExist(email); 
        map.put("isExist", isExist);
        return map;
    }
    
    // 닉네임 중복 확인
    @ResponseBody
    @PostMapping("/nicknameDuplicateCheck")
    public Map<String, Boolean> nicknameDuplicateCheck(@RequestParam("nickname") String nickname) { // ✅ 이름을 명시적으로 기재
        Map<String, Boolean> map = new HashMap<>();
        boolean isExist = memberService.isNicknameExist(nickname); 
        map.put("isExist", isExist);
        return map;
    }

    // 휴대폰 SMS 인증번호 발송
    @ResponseBody
    @PostMapping("/sendSms")
    public Map<String, Object> sendSms(@RequestParam("phone") String phone, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        
        // 휴대폰 번호 중복 검사
        if(memberService.isPhoneExist(phone)) {
            map.put("success", false);
            map.put("msg", "이미 가입된 휴대폰 번호입니다.");
            return map;
        }

        // 인증번호 생성 및 콘솔 출력
        String authCode = memberService.sendSms(phone);
        
        // 세션에 인증번호 저장
        session.setAttribute("smsAuthCode", authCode);
        
        map.put("success", true);
        map.put("msg", "인증번호가 발송되었습니다. 콘솔창을 확인하세요!");
        return map;
    }

    // 휴대폰 SMS 인증번호 확인
    @ResponseBody
    @PostMapping("/verifySms")
    public Map<String, Boolean> verifySms(@RequestParam("code") String code, HttpSession session) {
        Map<String, Boolean> map = new HashMap<>();
        String sessionCode = (String) session.getAttribute("smsAuthCode");
        
        boolean isMatch = (sessionCode != null && sessionCode.equals(code));
        if(isMatch) {
            session.removeAttribute("smsAuthCode"); 
        }
        
        map.put("isMatch", isMatch);
        return map;
    }

    // 회원가입 완료 처리
    @PostMapping("/registerEnd")
    public String registerEnd(MemberDTO memberDTO, HttpServletRequest request) {
        
        // 비밀번호 암호화 (Spring Security)
        memberDTO.setPassword(passwordEncoder.encode(memberDTO.getPassword()));
        
        // 프로필 이미지 파일 업로드 처리
        MultipartFile attach = memberDTO.getAttach();
        if (attach != null && !attach.isEmpty()) {
            try {
                String path = request.getSession().getServletContext().getRealPath("/resources/profile_images/");
                File dir = new File(path);
                if(!dir.exists()) dir.mkdirs();
                
                String originalFilename = attach.getOriginalFilename();
                String saveFilename = System.currentTimeMillis() + "_" + originalFilename;
                
                attach.transferTo(new File(path + saveFilename));
                memberDTO.setProfileImg(saveFilename);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            memberDTO.setProfileImg("default_profile.png"); // 기본 이미지
        }
        
        // DB 저장
        memberService.registerMember(memberDTO);
        
        return "redirect:/";
    }
    
    
}