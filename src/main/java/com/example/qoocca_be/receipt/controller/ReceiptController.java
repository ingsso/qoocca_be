package com.example.qoocca_be.receipt.controller;

import com.example.qoocca_be.receipt.model.*;
import com.example.qoocca_be.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student/{studentId}/receipt")
@RequiredArgsConstructor
public class ReceiptController {

    private final ReceiptService receiptService;

    /* =========================
     * POST: 수납 생성
     * ========================= */
    @PostMapping
    public ReceiptCreateResponse createReceipt(
            @PathVariable Long studentId,
            @RequestBody ReceiptCreateRequest request
    ) {
        return receiptService.createReceipt(studentId, request);
    }

    /* =========================
     * GET: 학생 전체 수납 조회
     * ========================= */
    @GetMapping
    public List<ReceiptResponse> getAllReceipts(@PathVariable Long studentId) {
        return receiptService.getReceiptsByStudent(studentId);
    }

    /* =========================
     * GET: 달별 수납 조회
     * 예시: /month?year=2026&month=1
     * ========================= */
    @GetMapping("/month")
    public List<ReceiptResponse> getMonthlyReceipts(
            @PathVariable Long studentId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return receiptService.getReceiptsByStudentAndMonth(studentId, year, month);
    }

    /* =========================
     * PUT: 수납 상태 변경 (예: 취소)
     * ========================= */
    @PutMapping("/{receiptId}")
    public ReceiptUpdateResponse updateReceiptStatus(
            @PathVariable Long studentId,
            @PathVariable Long receiptId,
            @RequestBody ReceiptUpdateRequest request
    ) {
        return receiptService.updateReceiptStatus(studentId, receiptId, request);
    }
}
