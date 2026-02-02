package com.qoocca.teachers.api.parent.auth.controller;

import com.qoocca.teachers.api.parent.auth.dto.ParentLoginRequest;
import com.qoocca.teachers.api.parent.auth.dto.ParentLoginResponse;
import com.qoocca.teachers.api.parent.auth.service.ParentAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Parent Auth API", description = "보호자 로그인 API")
@RestController
@RequestMapping("/api/parent/auth")
@RequiredArgsConstructor
public class ParentAuthController {

    private final ParentAuthService parentAuthService;

    @Operation(summary = "보호자 로그인", description = "보호자 계정으로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ParentLoginResponse> login(@Valid @RequestBody ParentLoginRequest request) {
        return ResponseEntity.ok(parentAuthService.login(request));
    }
}
