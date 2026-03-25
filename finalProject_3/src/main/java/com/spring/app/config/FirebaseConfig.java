package com.spring.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {
    @PostConstruct
    public void init() {
        try {
            FileInputStream serviceAccount = new FileInputStream("/home/ubuntu/secrets/serviceAccountKey.json");
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    // Firebase 콘솔 -> 프로젝트 설정 -> 리얼타임 데이터베이스에서 확인 가능한 URL
                    .setDatabaseUrl("https://used-trading-platform-default-rtdb.asia-southeast1.firebasedatabase.app/") 
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}