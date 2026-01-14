package com.example.qoocca_be.receipt.controller;

import com.example.qoocca_be.receipt.model.ClassPaymentSummaryResponse;
import com.example.qoocca_be.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/academy")
@RequiredArgsConstructor
public class ReceiptClassController {

    private final ReceiptService receiptService;

    @GetMapping("/{academyId}/receipt/class-summary")
    public List<ClassPaymentSummaryResponse> getClassReceiptSummary(
            @PathVariable Long academyId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        return receiptService.getClassReceiptSummary(academyId, year, month);
    }
}
