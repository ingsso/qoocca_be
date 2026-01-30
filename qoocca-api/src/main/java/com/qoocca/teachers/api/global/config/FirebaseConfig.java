package com.qoocca.teachers.api.global.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void init() {
        try {
            // ClassPathResource를 사용해서 stream으로 읽기
            ClassPathResource resource = new ClassPathResource("service-account.json");
            InputStream serviceAccount = resource.getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) { // 이미 초기화되어 있으면 중복 방지
                FirebaseApp.initializeApp(options);
                System.out.println("✅ Firebase initialized successfully");
            }

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("❌ Firebase initialization failed");
        }
    }
}
