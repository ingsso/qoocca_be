package com.qoocca.teachers.api.academy.controller;

import com.qoocca.teachers.api.academy.model.response.AcademyCheckResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyListResponse;
import com.qoocca.teachers.api.academy.service.AcademyAccountService;
import com.qoocca.teachers.auth.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "내 학원 API", description = "내 소속 학원/등록상태 조회 API")
@RestController
@RequiredArgsConstructor
public class MyAcademyController {

    private final AcademyAccountService academyAccountService;

    @Operation(summary = "내 학원 등록 상태 조회", description = "현재 로그인 사용자 기준 학원 등록/승인 상태를 조회합니다.")
    @GetMapping("/api/me/academy-registration")
    public ResponseEntity<AcademyCheckResponse> checkRegistration(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(academyAccountService.checkRegistrationStatus(userDetails.getUserId()));
    }

    @Operation(summary = "내 소속 학원 목록 조회", description = "현재 로그인 사용자가 소속된 학원 목록을 조회합니다.")
    @GetMapping("/api/me/academies")
    public ResponseEntity<List<AcademyListResponse>> getMyAcademyList(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        return ResponseEntity.ok(academyAccountService.getMyAcademies(userDetails.getUserId()));
    }
}