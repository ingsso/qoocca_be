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

    // 1截뤴깵 ?대씪?댁뼵?몄뿉??諛쏆? ?좏겙 ???媛깆떊
    public void registerToken(Long parentId, String token) {
        Optional<FirebaseTokenEntity> existing = tokenRepository.findByParentId(parentId);
        FirebaseTokenEntity entity = existing.orElseGet(FirebaseTokenEntity::new);
        entity.setParentId(parentId);
        entity.setFcmToken(token);
        tokenRepository.save(entity);
    }

    // 2截뤴깵 ?좎??먭쾶 ?몄떆 蹂대궡湲?
    public void sendPushToUser(Long parentId, Long receiptId, String title, String body) {
        tokenRepository.findByParentId(parentId).ifPresent(token -> {
            sendPush(token.getFcmToken(), receiptId, title, body);
        });
    }

    // 3截뤴깵 ?ㅼ젣 FCM ?꾩넚
    private void sendPush(String token, Long receiptId, String title, String body) {
        Message message = Message.builder()
                .setToken(token)
                .putData("receiptId", receiptId != null ? receiptId.toString() : "")
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
