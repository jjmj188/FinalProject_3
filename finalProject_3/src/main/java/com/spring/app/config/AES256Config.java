package com.spring.app.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.spring.app.common.AES256;

@Configuration
public class AES256Config {

    @Value("${aes256.secret-key}")
    private String secretKey;

    @Bean
    public AES256 aes256() throws Exception {
        return new AES256(secretKey);
    }
}
