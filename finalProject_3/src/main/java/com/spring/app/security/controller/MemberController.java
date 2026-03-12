package com.spring.app.security.controller;

import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.spring.app.common.FileManager;
import com.spring.app.security.domain.MemberDTO;
import com.spring.app.security.service.MemberService;
import com.spring.app.security.service.SmsService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/security")
public class MemberController {
    
    @Autowired
    private MemberService memberService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    // (새로 추가) CoolSMS를 통한 실제 문자 발송 서비스 주입
    @Autowired
    private SmsService smsService;

    @Autowired
    private FileManager fileManager;

    @Value("${file.profile-dir}")
    private String profileDir;

    @GetMapping("/login")
    public String loginForm() {
        return "security/login"; // templates/security/login.html
    }

    @GetMapping("/register")
    public String registerForm() {
        return "security/register";
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
    public Map<String, Boolean> nicknameDuplicateCheck(@RequestParam("nickname") String nickname) { 
        Map<String, Boolean> map = new HashMap<>();
        boolean isExist = memberService.isNicknameExist(nickname); 
        map.put("isExist", isExist);
        return map;
    }

    // 휴대폰 SMS 인증번호 발송 (CoolSMS 연동 + 세션 저장)
    @ResponseBody
    @PostMapping("/sendSms")
    public Map<String, Object> sendSms(@RequestParam("phone") String phone, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        
        // 1. 휴대폰 번호 중복 검사 (이미 가입된 번호인지 확인)
        if(memberService.isPhoneExist(phone)) {
            map.put("success", false);
            map.put("msg", "이미 가입된 휴대폰 번호입니다.");
            return map;
        }

        try {
            // 2. 6자리 난수(인증번호) 생성
            Random rand = new Random();
            StringBuilder numStr = new StringBuilder();
            for(int i = 0; i < 6; i++) {
                String ran = Integer.toString(rand.nextInt(10));
                numStr.append(ran);
            }
            String verificationCode = numStr.toString();

            // CoolSMS 서비스 호출하여 실제 문자 발송
            smsService.sendSms(phone, verificationCode);
            
            // 세션에 인증번호 저장
            session.setAttribute("smsAuthCode", verificationCode);
            session.setMaxInactiveInterval(3 * 60); // 세션 만료 시간 3분 설정
            
            map.put("success", true);
            map.put("msg", "인증번호가 발송되었습니다.");

        } catch (Exception e) {
            e.printStackTrace();
            map.put("success", false);
            map.put("msg", "문자 발송에 실패했습니다. 관리자에게 문의하세요.");
        }

        return map;
    }
    
    // 휴대폰 SMS 인증번호 확인
    @ResponseBody
    @PostMapping("/verifySms")
    public Map<String, Boolean> verifySms(@RequestParam("code") String code, HttpSession session) {
        Map<String, Boolean> map = new HashMap<>();
        
        // 세션에서 저장해둔 인증번호 꺼내기
        String sessionCode = (String) session.getAttribute("smsAuthCode");
        
        // 사용자가 입력한 코드와 세션의 코드가 일치하는지 확인
        boolean isMatch = (sessionCode != null && sessionCode.equals(code));
        
        if(isMatch) {
            // 일치하면 세션에서 인증번호 삭제
            session.removeAttribute("smsAuthCode"); 
        }
        
        map.put("isMatch", isMatch);
        return map;
    }

    // 회원가입 완료 처리
    @PostMapping("/registerEnd")
    public String registerEnd(MemberDTO memberDTO) {

        // 비밀번호 암호화 (Spring Security)
        memberDTO.setPassword(passwordEncoder.encode(memberDTO.getPassword()));

        MultipartFile attach = memberDTO.getAttach();

        if (attach != null && !attach.isEmpty()) {
            // ① 내 PC에서 사진 올리기 — FileManager로 file_profile/ 에 저장
            try {
                String savedName = fileManager.doFileUpload(
                        attach.getBytes(),
                        attach.getOriginalFilename(),
                        profileDir
                );
                memberDTO.setProfileImg(savedName != null ? savedName : "default_profile.png");
            } catch (Exception e) {
                e.printStackTrace();
                memberDTO.setProfileImg("default_profile.png");
            }

        } else if (memberDTO.getDefaultProfile() != null && !memberDTO.getDefaultProfile().isEmpty()) {
            // ② 일러스트(dicebear API URL) 선택 — URL 스트림을 FileManager로 file_profile/ 에 저장
            try {
                URL url = new URL(memberDTO.getDefaultProfile());
                try (InputStream in = url.openStream()) {
                    String savedName = fileManager.doFileUpload(in, "avatar.svg", profileDir);
                    memberDTO.setProfileImg(savedName != null ? savedName : "default_profile.png");
                }
            } catch (Exception e) {
                e.printStackTrace();
                memberDTO.setProfileImg("default_profile.png");
            }

        } else {
            // ③ 아무것도 선택 안 한 경우 — 기본 이미지
            memberDTO.setProfileImg("default_profile.png");
        }

        // DB 저장
        memberService.registerMember(memberDTO);

        return "redirect:/";
    }
}