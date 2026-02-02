package com.qoocca.teachers.api.global.controller;

import com.qoocca.teachers.api.global.service.FcmPushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "FCM API", description = "부모 앱 푸시 토큰 등록 API")
@RestController
@RequestMapping("/api/fcm")
public class FcmController {

    private final FcmPushService fcmService;

    public FcmController(FcmPushService fcmService) {
        this.fcmService = fcmService;
    }

    @Operation(summary = "FCM 토큰 등록", description = "부모의 FCM 토큰을 등록하거나 갱신합니다.")
    @PostMapping("/register")
    public String registerToken(
            @Parameter(description = "부모 ID", example = "1") @RequestParam Long parentId,
            @Parameter(description = "FCM 디바이스 토큰") @RequestParam String fcmToken) {
        fcmService.registerToken(parentId, fcmToken);
        return "FCM token registered";
    }
}
