package com.qoocca.teachers.api.receipt.controller;

import com.qoocca.teachers.api.receipt.model.response.ClassPaymentSummaryResponse;
import com.qoocca.teachers.api.receipt.model.response.DashboardMainSummaryResponse;
import com.qoocca.teachers.api.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "수납 요약 API", description = "학원 대시보드 수납 요약 API")
@RestController
@RequestMapping("/api/academy")
@RequiredArgsConstructor
public class ReceiptClassController {

    private final ReceiptService receiptService;

    @Operation(summary = "반별 수납 요약 조회", description = "지정 연/월 기준 반별 수납 요약 정보를 조회합니다.")
    @GetMapping("/{academyId}/dashboard/receipt-class-summary")
    public List<ClassPaymentSummaryResponse> getClassReceiptSummary(
            @Parameter(description = "학원 ID", example = "1") @PathVariable Long academyId,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam int year,
            @Parameter(description = "조회 월", example = "1") @RequestParam int month
    ) {
        return receiptService.getClassReceiptSummary(academyId, year, month);
    }

    @Operation(summary = "대시보드 메인 수납 요약 조회", description = "지정 연/월 기준 대시보드 메인 수납 요약 정보를 조회합니다.")
    @GetMapping("/{academyId}/dashboard/receipt-main")
    public ResponseEntity<List<DashboardMainSummaryResponse>> getDashboardMainSummary(
            @Parameter(description = "학원 ID", example = "1") @PathVariable Long academyId,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam int year,
            @Parameter(description = "조회 월", example = "1") @RequestParam int month
    ) {
        return ResponseEntity.ok(receiptService.getDashboardMainSummary(academyId, year, month));
    }
}
