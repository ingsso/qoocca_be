package com.qoocca.teachers.api.receipt.controller;

import com.qoocca.teachers.api.receipt.model.response.ReceiptUpdateResponse;
import com.qoocca.teachers.api.receipt.service.ReceiptService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
public class ReceiptPaymentController {

    private final ReceiptService receiptService;

    @PostMapping("/{receiptId}/pay")
    public ResponseEntity<ReceiptUpdateResponse> payReceipt(@PathVariable Long receiptId,
                                                            @RequestParam Long parentId) {
        return ResponseEntity.ok(receiptService.payReceipt(receiptId, parentId));
    }

    @PostMapping("/{receiptId}/cancel")
    public ResponseEntity<ReceiptUpdateResponse> cancelReceipt(@PathVariable Long receiptId,
                                                               @RequestParam Long parentId) {
        return ResponseEntity.ok(receiptService.cancelReceipt(receiptId, parentId));
    }
}
