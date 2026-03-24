package com.spring.app.security.service;

import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.util.HashMap;

@Service
public class SmsService {
    @Value("${coolsms.api-key}")
    private String api_key;

    @Value("${coolsms.api-secret}")
    private String api_secret;

    public void sendSms(String to, String randomNumber) {
        Message coolsms = new Message(api_key, api_secret);

        HashMap<String, String> params = new HashMap<>();
        params.put("to", to);    // 수신 전화번호
        params.put("from", "01045261348"); // CoolSMS에 등록된 발신번호
        params.put("type", "SMS");
        params.put("text", "[인증번호] " + randomNumber + " 를 입력해주세요.");

        try {
            coolsms.send(params);
        } catch (CoolsmsException e) {
        }
    }
}