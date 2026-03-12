package com.spring.app.security.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.spring.app.security.service.MemberService;
import com.spring.app.security.service.SmsService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/security")
public class MemberFindController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private SmsService smsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // 1. 계정 찾기용 SMS 발송 (가입된 번호인지 확인 후 발송)
    @ResponseBody
    @PostMapping("/sendSmsForFind")
    public Map<String, Object> sendSmsForFind(@RequestParam("phone") String phone, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        
        // 가입된 번호가 아니면 에러 반환
        if(!memberService.isPhoneExist(phone)) {
            map.put("success", false);
            map.put("msg", "해당 번호로 가입된 계정이 없습니다.");
            return map;
        }

        try {
            Random rand = new Random();
            StringBuilder numStr = new StringBuilder();
            for(int i = 0; i < 6; i++) {
                numStr.append(rand.nextInt(10));
            }
            String verificationCode = numStr.toString();

            // CoolSMS 문자 발송
            smsService.sendSms(phone, verificationCode);
            
            // 세션에 저장 (회원가입과 구분되는 키 사용)
            session.setAttribute("smsFindCode", verificationCode);
            session.setMaxInactiveInterval(3 * 60); // 3분
            
            map.put("success", true);
            map.put("msg", "인증번호가 발송되었습니다.");
        } catch (Exception e) {
            map.put("success", false);
            map.put("msg", "문자 발송에 실패했습니다.");
        }
        return map;
    }

    // 2. 이메일 찾기 (인증번호 검증 후 이메일 반환)
    @ResponseBody
    @PostMapping("/findEmail")
    public Map<String, Object> findEmail(@RequestParam("phone") String phone, @RequestParam("code") String code, HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        String sessionCode = (String) session.getAttribute("smsFindCode");
        
        if(sessionCode != null && sessionCode.equals(code)) {
            // 인증 성공: DB에서 이메일 가져오기
            String email = memberService.findEmailByPhone(phone);
            session.removeAttribute("smsFindCode"); // 세션 초기화
            map.put("success", true);
            map.put("email", email);
        } else {
            map.put("success", false);
            map.put("msg", "인증번호가 일치하지 않습니다.");
        }
        return map;
    }

    // 4. 휴면 계정 해제용 SMS 발송
    @ResponseBody
    @PostMapping("/sendSmsForIdle")
    public Map<String, Object> sendSmsForIdle(@RequestParam("phone") String phone, HttpSession session) {
        Map<String, Object> map = new HashMap<>();

        if (!memberService.isIdlePhone(phone)) {
            map.put("success", false);
            map.put("msg", "해당 번호로 등록된 휴면 계정이 없습니다.");
            return map;
        }

        try {
            Random rand = new Random();
            StringBuilder numStr = new StringBuilder();
            for (int i = 0; i < 6; i++) numStr.append(rand.nextInt(10));
            String verificationCode = numStr.toString();

            smsService.sendSms(phone, verificationCode);

            session.setAttribute("smsIdleCode", verificationCode);
            session.setAttribute("smsIdlePhone", phone);
            session.setMaxInactiveInterval(3 * 60);

            map.put("success", true);
            map.put("msg", "인증번호가 발송되었습니다.");
        } catch (Exception e) {
            map.put("success", false);
            map.put("msg", "문자 발송에 실패했습니다.");
        }
        return map;
    }

    // 5. 휴면 해제 처리 (인증번호 확인 후 IDLE=0, USER_DORMANT 삭제)
    @ResponseBody
    @PostMapping("/reactivateMember")
    public Map<String, Object> reactivateMember(@RequestParam("phone") String phone,
                                                 @RequestParam("code") String code,
                                                 HttpSession session) {
        Map<String, Object> map = new HashMap<>();
        String sessionCode  = (String) session.getAttribute("smsIdleCode");
        String sessionPhone = (String) session.getAttribute("smsIdlePhone");

        if (sessionCode == null || !sessionCode.equals(code) || !phone.equals(sessionPhone)) {
            map.put("success", false);
            map.put("msg", "인증번호가 일치하지 않습니다.");
            return map;
        }

        try {
            String email = memberService.findEmailByPhone(phone);
            if (email == null) {
                map.put("success", false);
                map.put("msg", "계정 정보를 찾을 수 없습니다.");
                return map;
            }

            memberService.reactivateMember(email);
            session.removeAttribute("smsIdleCode");
            session.removeAttribute("smsIdlePhone");

            map.put("success", true);
            map.put("msg", "휴면 해제가 완료되었습니다. 다시 로그인해주세요.");
        } catch (Exception e) {
            map.put("success", false);
            map.put("msg", "휴면 해제 중 오류가 발생했습니다.");
        }
        return map;
    }

    // 3. 비밀번호 재설정 (새 비밀번호로 DB 업데이트)
    @ResponseBody
    @PostMapping("/resetPassword")
    public Map<String, Object> resetPassword(@RequestParam("phone") String phone, @RequestParam("newPassword") String newPassword) {
        Map<String, Object> map = new HashMap<>();
        
        try {
            // 새 비밀번호를 Spring Security 암호화 방식으로 인코딩
            String encodedPassword = passwordEncoder.encode(newPassword);
            
            // DB 업데이트
            memberService.updatePasswordByPhone(phone, encodedPassword);
            
            map.put("success", true);
            map.put("msg", "비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요.");
        } catch(Exception e) {
            map.put("success", false);
            map.put("msg", "비밀번호 변경 중 오류가 발생했습니다.");
        }
        return map;
    }
}