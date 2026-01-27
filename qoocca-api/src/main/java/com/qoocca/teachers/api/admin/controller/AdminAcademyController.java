package com.qoocca.teachers.api.admin.controller;

import com.qoocca.teachers.api.academy.model.request.AcademyRejectRequest;
import com.qoocca.teachers.api.academy.model.response.AcademyListResponse;
import com.qoocca.teachers.api.academy.service.AcademyService;
import com.qoocca.teachers.common.global.common.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin API", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminAcademyController {

    private final AcademyService academyService;

    @Operation(summary = "학원 등록 승인")
    @PostMapping("/academy/{id}/approve")
    public ResponseEntity<String> approveAcademy(@PathVariable Long id) {
        academyService.approveAcademy(id);
        return ResponseEntity.ok("학원 승인이 완료되었습니다.");
    }

    @Operation(summary = "학원 등록 반려")
    @PostMapping("/academy/{id}/reject")
    public ResponseEntity<String> rejectAcademy(
            @PathVariable Long id,
            @RequestBody @Valid AcademyRejectRequest request
    ) {
        academyService.rejectAcademy(id, request.getRejectionReason());
        return ResponseEntity.ok("학원 승인이 반려되었습니다.");
    }

    @Operation(summary = "승인 대기 중인 학원 리스트 조회")
    @GetMapping("/academy/pending")
    public ResponseEntity<PageResponse<AcademyListResponse>> getPendingAcademies(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(academyService.getPendingAcademies(pageable));
    }

    @Operation(summary = "반려 학원 리스트 조회")
    @GetMapping("/academy/rejected")
    public ResponseEntity<PageResponse<AcademyListResponse>> getRejectedAcademies(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(academyService.getRejectedAcademies(pageable));
    }
}
