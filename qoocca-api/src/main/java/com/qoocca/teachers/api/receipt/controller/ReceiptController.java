package com.qoocca.teachers.api.receipt.controller;

import com.qoocca.teachers.api.receipt.model.ReceiptCreateRequest;
import com.qoocca.teachers.api.receipt.model.ReceiptUpdateRequest;
import com.qoocca.teachers.api.receipt.model.response.ReceiptCreateResponse;
import com.qoocca.teachers.api.receipt.model.response.ReceiptResponse;
import com.qoocca.teachers.api.receipt.model.response.ReceiptUpdateResponse;
import com.qoocca.teachers.api.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Student Receipt API", description = "학생 수납 생성/조회/수정 API")
@RestController
@RequestMapping("/api/student/{studentId}/receipt")
@RequiredArgsConstructor
public class ReceiptController {


    private final ReceiptService receiptService;

    @Operation(summary = "수납 기록 생성", description = "학생 수납(결제) 기록을 생성합니다.")
    @PostMapping
    public ReceiptCreateResponse createReceipt(
            @Parameter(description = "Student ID", example = "1") @PathVariable Long studentId,
            @RequestBody ReceiptCreateRequest request
    ) {
        return receiptService.createReceipt(studentId, request);
    }

    @Operation(summary = "학생 전체 수납 이력 조회", description = "특정 학생의 전체 수납 이력을 조회합니다.")
    @GetMapping
    public List<ReceiptResponse> getAllReceipts(
            @Parameter(description = "Student ID", example = "1") @PathVariable Long studentId) {
        return receiptService.getReceiptsByStudent(studentId);
    }

    @Operation(summary = "학생 월별 수납 이력 조회", description = "연도/월을 지정하여 학생 수납 이력을 조회합니다.")
    @GetMapping("/month")
    public List<ReceiptResponse> getMonthlyReceipts(
            @Parameter(description = "Student ID", example = "1") @PathVariable Long studentId,
            @Parameter(description = "Year", example = "2026") @RequestParam int year,
            @Parameter(description = "Month", example = "1") @RequestParam int month
    ) {
        return receiptService.getReceiptsByStudentAndMonth(studentId, year, month);
    }

    @Operation(summary = "수납 상태 수정", description = "수납 상태를 수정합니다.")
    @PutMapping("/{receiptId}")
    public ReceiptUpdateResponse updateReceiptStatus(
            @Parameter(description = "Student ID", example = "1") @PathVariable Long studentId,
            @Parameter(description = "Receipt ID", example = "100") @PathVariable Long receiptId,
            @RequestBody ReceiptUpdateRequest request
    ) {
        return receiptService.updateReceiptStatus(studentId, receiptId, request);
    }
}
