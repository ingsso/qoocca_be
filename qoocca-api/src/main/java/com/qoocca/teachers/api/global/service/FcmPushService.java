package com.qoocca.teachers.api.global.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.qoocca.teachers.db.fcm.FirebaseTokenEntity;
import com.qoocca.teachers.db.fcm.FirebaseTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
            System.out.println("??Sent message: " + response);
        } catch (FirebaseMessagingException e) {
            e.printStackTrace();
        }
    }
}
