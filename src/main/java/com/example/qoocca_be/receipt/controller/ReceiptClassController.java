package com.example.qoocca_be.receipt.controller;

import com.example.qoocca_be.receipt.model.response.ClassPaymentSummaryResponse;
import com.example.qoocca_be.receipt.model.response.DashboardMainSummaryResponse;
import com.example.qoocca_be.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Receipt Management API", description = "학원 수납 현황 관리 및 대시보드 통계 API")
@RestController
@RequestMapping("/api/academy")
@RequiredArgsConstructor
public class ReceiptClassController {

    private final ReceiptService receiptService;

    @Operation(
            summary = "학원별 클래스 수납 요약 조회",
            description = "특정 연월의 학원 내 클래스별 수납 상태(요청 전, 대기, 완료) 인원 수와 학생 상세 내역을 조회합니다."
    )
    @GetMapping("/{academyId}/receipt/class-summary")
    public List<ClassPaymentSummaryResponse> getClassReceiptSummary(
            @Parameter(description = "학원 ID", example = "1") @PathVariable Long academyId,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam int year,
            @Parameter(description = "조회 월", example = "1") @RequestParam int month
    ) {
        return receiptService.getClassReceiptSummary(academyId, year, month);
    }

    @Operation(
            summary = "메인 대시보드용 수납 요약 조회",
            description = "대시보드 메인 화면에 표시할 클래스별 수납 대표 상태 및 총 수납 예정 금액을 조회합니다."
    )
    @GetMapping("/{academyId}/receipt/dashboard-main")
    public ResponseEntity<List<DashboardMainSummaryResponse>> getDashboardMainSummary(
            @Parameter(description = "학원 ID", example = "1") @PathVariable Long academyId,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam int year,
            @Parameter(description = "조회 월", example = "1") @RequestParam int month
    ) {
        return ResponseEntity.ok(receiptService.getDashboardMainSummary(academyId, year, month));
    }
}