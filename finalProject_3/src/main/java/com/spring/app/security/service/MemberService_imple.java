package com.spring.app.security.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.spring.app.common.AES256;
import com.spring.app.security.domain.MemberDTO;
import com.spring.app.security.model.MemberDAO;

@Service
public class MemberService_imple implements MemberService {

    @Autowired
    private MemberDAO dao;

    @Autowired
    private AES256 aes256;

    @Override
    public boolean isEmailExist(String email) {
        return dao.checkEmail(email) > 0;
    }

    @Override
    public boolean isNicknameExist(String nickname) {
        return dao.checkNickname(nickname) > 0;
    }

    @Override
    public boolean isPhoneExist(String phone) {
        try {
            return dao.checkPhone(aes256.encrypt(phone)) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void registerMember(MemberDTO member) {
        try {
            member.setPhone(aes256.encrypt(member.getPhone()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        dao.insertMember(member);
    }

    @Override
    public String sendSms(String phone) {
        Random random = new Random();
        String authCode = String.format("%06d", random.nextInt(1000000));
        // 처음엔 일단 인증번호 콘솔로 전송 추후 api연결 예정
        System.out.println("========================================");
        System.out.println("수신번호: " + phone + " / 생성된 인증번호: " + authCode);
        System.out.println("========================================");

        // TODO: 향후 CoolSMS 등 실제 API 연동 시 여기에 코드 추가
        return authCode;
    }

    @Override
    public String findEmailByPhone(String phone) {
        try {
            return dao.findEmailByPhone(aes256.encrypt(phone));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void updatePasswordByPhone(String phone, String encodedPassword) {
        try {
            Map<String, String> paramMap = new HashMap<>();
            paramMap.put("phone", aes256.encrypt(phone));
            paramMap.put("password", encodedPassword);
            dao.updatePasswordByPhone(paramMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isIdlePhone(String phone) {
        try {
            return dao.checkIdlePhone(aes256.encrypt(phone)) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void reactivateMember(String email) {
        dao.reactivateMember(email);
        dao.deleteFromUserDormant(email);
    }
}