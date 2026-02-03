package com.qoocca.teachers.api.global.service;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.qoocca.teachers.db.fcm.FirebaseTokenEntity;
import com.qoocca.teachers.db.fcm.FirebaseTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class FcmPushService {

    private final FirebaseTokenRepository tokenRepository;

    public FcmPushService(FirebaseTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    public void registerToken(Long parentId, String token) {
        Optional<FirebaseTokenEntity> existing = tokenRepository.findByParentId(parentId);
        FirebaseTokenEntity entity = existing.orElseGet(FirebaseTokenEntity::new);
        entity.setParentId(parentId);
        entity.setFcmToken(token);
        tokenRepository.save(entity);
    }

    public void sendPushToUser(Long parentId, Long receiptId, String title, String body) {
        tokenRepository.findByParentId(parentId).ifPresent(token -> {
            sendPush(token.getFcmToken(), receiptId, title, body);
        });
    }

    private void sendPush(String token, Long receiptId, String title, String body) {
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Skipping FCM push because Firebase is not initialized. receiptId={}", receiptId);
            return;
        }

        String receiptIdValue = receiptId != null ? receiptId.toString() : "";
        String receiptPath = receiptId != null ? "/api/parent/receipt/" + receiptId : "/api/parent/receipt/requests";

        Message message = Message.builder()
                .setToken(token)
                .putData("type", "RECEIPT_ISSUED")
                .putData("receiptId", receiptIdValue)
                .putData("route", receiptPath)
                .putData("title", title != null ? title : "")
                .putData("body", body != null ? body : "")
                .build();

        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM push sent. receiptId={}, responseId={}", receiptId, response);
        } catch (FirebaseMessagingException e) {
            log.warn("FCM push failed. receiptId={}, reason={}", receiptId, e.getMessage());
        }
    }
}
