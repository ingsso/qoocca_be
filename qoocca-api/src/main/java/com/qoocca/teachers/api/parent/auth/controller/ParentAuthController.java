package com.qoocca.teachers.api.parent.auth.controller;

import com.qoocca.teachers.api.parent.auth.dto.ParentLoginRequest;
import com.qoocca.teachers.api.parent.auth.dto.ParentLoginResponse;
import com.qoocca.teachers.api.parent.auth.service.ParentAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parent/auth")
@RequiredArgsConstructor
public class ParentAuthController {

    private final ParentAuthService parentAuthService;

    @PostMapping("/login")
    public ResponseEntity<ParentLoginResponse> login(@Valid @RequestBody ParentLoginRequest request) {
        return ResponseEntity.ok(parentAuthService.login(request));
    }
}
