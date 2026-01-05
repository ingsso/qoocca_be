package com.example.qoocca_be.user.controller;

import com.example.qoocca_be.academy.service.AcademyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin API", description = "관리자 전용 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AcademyService academyService;

    @Operation(summary = "학원 등록 승인")
    @PostMapping("/{id}/approve")
    public ResponseEntity<String> approveAcademy(@PathVariable Long id) {
        academyService.approveAcademy(id);
        return ResponseEntity.ok("학원 승인이 완료되었습니다.");
    }
}
