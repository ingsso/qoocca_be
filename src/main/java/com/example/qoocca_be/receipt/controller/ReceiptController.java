package com.example.qoocca_be.receipt.controller;

import com.example.qoocca_be.receipt.model.*;
import com.example.qoocca_be.receipt.model.response.ReceiptCreateResponse;
import com.example.qoocca_be.receipt.model.response.ReceiptResponse;
import com.example.qoocca_be.receipt.model.response.ReceiptUpdateResponse;
import com.example.qoocca_be.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Student Receipt API", description = "학생 개인별 수납 내역 등록, 수정 및 상세 조회 API")
@RestController
@RequestMapping("/api/student/{studentId}/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    @Operation(summary = "수납 기록 생성", description = "특정 학생의 수업에 대한 수납(결제 요청/완료) 기록을 생성합니다. 한 달에 한 번만 생성이 가능하도록 제한됩니다.")
    @PostMapping
    public ReceiptCreateResponse createReceipt(
            @Parameter(description = "학생 ID", example = "1") @PathVariable Long studentId,
            @RequestBody ReceiptCreateRequest request
    ) {
        return receiptService.createReceipt(studentId, request);
    }

    @Operation(summary = "학생 전체 수납 이력 조회", description = "특정 학생이 지금까지 결제했거나 청구된 모든 수납 내역을 조회합니다.")
    @GetMapping
    public List<ReceiptResponse> getAllReceipts(
            @Parameter(description = "학생 ID", example = "1") @PathVariable Long studentId) {
        return receiptService.getReceiptsByStudent(studentId);
    }

    @Operation(summary = "학생 월별 수납 내역 조회", description = "연도와 월을 지정하여 해당 기간에 발생한 학생의 수납 내역을 조회합니다.")
    @GetMapping("/month")
    public List<ReceiptResponse> getMonthlyReceipts(
            @Parameter(description = "학생 ID", example = "1") @PathVariable Long studentId,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam int year,
            @Parameter(description = "조회 월", example = "1") @RequestParam int month
    ) {
        return receiptService.getReceiptsByStudentAndMonth(studentId, year, month);
    }

    @Operation(summary = "수납 상태 수정", description = "발행된 영수증의 상태(결제 완료, 수납 취소 등)를 업데이트합니다.")
    @PutMapping("/{receiptId}")
    public ReceiptUpdateResponse updateReceiptStatus(
            @Parameter(description = "학생 ID", example = "1") @PathVariable Long studentId,
            @Parameter(description = "영수증(수납) ID", example = "100") @PathVariable Long receiptId,
            @RequestBody ReceiptUpdateRequest request
    ) {
        return receiptService.updateReceiptStatus(studentId, receiptId, request);
    }
}