package com.qoocca.teachers.api.global.controller;

import com.qoocca.teachers.api.global.service.FcmPushService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fcm")
public class FcmController {

    private final FcmPushService fcmService;

    public FcmController(FcmPushService fcmService) {
        this.fcmService = fcmService;
    }

    @PostMapping("/register")
    public String registerToken(@RequestParam Long parentId, @RequestParam String fcmToken) {
        fcmService.registerToken(parentId, fcmToken);
        return "FCM token registered";
    }
}
