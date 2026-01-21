package com.example.qoocca_be.academy.controller;

import com.example.qoocca_be.academy.model.request.AcademyCreateRequest;
import com.example.qoocca_be.academy.model.request.AcademyUpdateRequest;
import com.example.qoocca_be.academy.model.response.AcademyCheckResponse;
import com.example.qoocca_be.academy.model.response.AcademyListResponse;
import com.example.qoocca_be.academy.model.response.AcademyResponse;
import com.example.qoocca_be.academy.model.response.DashboardStatsResponse;
import com.example.qoocca_be.academy.service.AcademyService;
import com.example.qoocca_be.age.model.AgeResponse;
import com.example.qoocca_be.subject.model.SubjectResponse;
import com.example.qoocca_be.user.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Academy API", description = "학원 정보 관리 및 운영 현황 조회 API")
@RestController
@RequestMapping("/api/academy")
@RequiredArgsConstructor
public class AcademyController {

    private final AcademyService academyService;

    @Operation(summary = "학원 등록", description = "학원 정보 및 사업자 등록증, 학원 이미지를 등록합니다. multipart/form-data 형식을 사용합니다.")
    @PostMapping(value = "/register", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Long> registerAcademy(
            @Valid @ModelAttribute AcademyCreateRequest req,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = academyService.registerAcademy(req, userDetails.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @Operation(summary = "학원 승인 여부 및 대표 ID 확인", description = "현재 로그인한 유저의 학원 승인 상태와 대표 학원 ID를 반환합니다.")
    @GetMapping("/check-registration")
    public ResponseEntity<AcademyCheckResponse> checkRegistration(@Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        AcademyCheckResponse res = academyService.checkRegistrationStatus(userDetails.getUserId());
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "특정 학원 상세 정보 조회", description = "학원 ID를 통해 학원의 상세 프로필 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<AcademyResponse> getAcademyDetail(
            @Parameter(description = "학원 고유 ID", example = "1") @PathVariable Long id) {
        AcademyResponse res = academyService.getAcademyDetail(id);
        return ResponseEntity.ok(res);
    }

    @Operation(summary = "특정 학원의 과목 리스트 조회")
    @GetMapping("/{id}/subjects")
    public ResponseEntity<List<SubjectResponse>> getSubjects(
            @Parameter(description = "학원 고유 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(academyService.getAcademySubjects(id));
    }

    @Operation(summary = "특정 학원의 연령대 리스트 조회")
    @GetMapping("/{id}/ages")
    public ResponseEntity<List<AgeResponse>> getAges(
            @Parameter(description = "학원 고유 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(academyService.getAcademyAges(id));
    }

    @Operation(summary = "학원 정보 수정", description = "학원의 기본 정보를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<Void> updateAcademy(
            @Parameter(description = "수정할 학원 고유 ID", example = "1") @PathVariable Long id,
            @RequestBody AcademyUpdateRequest dto,
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        academyService.updateAcademy(id, dto, userDetails.getUserId());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "학원 대시보드 운영 현황", description = "오늘의 출석, 원생 수, 미수납 내역 등 통계 데이터를 집계합니다.")
    @GetMapping("/{id}/stats")
    public ResponseEntity<DashboardStatsResponse> getDashboardStats(
            @Parameter(description = "학원 고유 ID", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(academyService.getDashboardStats(id));
    }

    @Operation(summary = "유저 소유 학원 리스트 조회", description = "현재 유저가 등록한 모든 학원의 ID, 이름, 승인 상태 리스트를 반환합니다.")
    @GetMapping("/academy-list")
    public ResponseEntity<List<AcademyListResponse>> getMyAcademyList(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(academyService.getMyAcademies(userDetails.getUserId()));
    }
}
