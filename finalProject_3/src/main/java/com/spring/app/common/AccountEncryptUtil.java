package com.spring.app.common;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class AccountEncryptUtil {

    private static String SECRET_KEY;

    @Value("${account.encryption.key}")
    public void setSecretKey(String key) {
        // AES-128은 정확히 16바이트 필요 → 부족하면 0으로 패딩, 넘치면 잘라냄
        byte[] keyBytes = new byte[16];
        byte[] raw = key.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        System.arraycopy(raw, 0, keyBytes, 0, Math.min(raw.length, 16));
        SECRET_KEY = new String(keyBytes, java.nio.charset.StandardCharsets.ISO_8859_1);
    }

    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) return plainText;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("계좌번호 암호화 실패", e);
        }
    }

    public static String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) return cipherText;
        try {
            SecretKeySpec keySpec = new SecretKeySpec(SECRET_KEY.getBytes(java.nio.charset.StandardCharsets.ISO_8859_1), "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), "UTF-8");
        } catch (Exception e) {
            // 복호화 실패 시 원문 반환 (기존 미암호화 데이터 호환)
            return cipherText;
        }
    }
}
