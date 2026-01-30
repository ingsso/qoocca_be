package com.qoocca.teachers.api.receipt.controller;

import com.qoocca.teachers.api.receipt.model.response.ReceiptUpdateResponse;
import com.qoocca.teachers.api.receipt.service.ReceiptService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Receipt Payment API", description = "수납 결제 처리 API")
@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
public class ReceiptPaymentController {

    private final ReceiptService receiptService;

    @Operation(summary = "수납 결제 완료", description = "수납 결제를 완료 처리합니다.")
    @PostMapping("/{receiptId}/pay")
    public ResponseEntity<ReceiptUpdateResponse> payReceipt(@PathVariable Long receiptId,
                                                            @RequestParam Long parentId) {
        return ResponseEntity.ok(receiptService.payReceipt(receiptId, parentId));
    }

    @Operation(summary = "수납 결제 취소", description = "수납 결제를 취소 처리합니다.")
    @PostMapping("/{receiptId}/cancel")
    public ResponseEntity<ReceiptUpdateResponse> cancelReceipt(@PathVariable Long receiptId,
                                                               @RequestParam Long parentId) {
        return ResponseEntity.ok(receiptService.cancelReceipt(receiptId, parentId));
    }
}
