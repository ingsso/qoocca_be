package com.example.qoocca_be.admin.controller;

import com.example.qoocca_be.academy.dto.AcademySearchResponseDto;
import com.example.qoocca_be.academy.service.AcademyService;
import com.example.qoocca_be.global.common.PageResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    public ResponseEntity<String> rejectAcademy(@PathVariable Long id) {
        academyService.rejectAcademy(id);
        return ResponseEntity.ok("학원 승인이 반려되었습니다.");
    }

    @Operation(summary = "승인 대기 중인 학원 리스트 조회")
    @GetMapping("/pending")
    public ResponseEntity<PageResponseDto<AcademySearchResponseDto>> getPendingAcademies(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {

        return ResponseEntity.ok(academyService.getPendingAcademies(pageable));
    }
}
