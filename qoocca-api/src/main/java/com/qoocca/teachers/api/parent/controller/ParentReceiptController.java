package com.qoocca.teachers.api.parent.controller;

import com.qoocca.teachers.api.receipt.model.response.ParentReceiptResponse;
import com.qoocca.teachers.api.receipt.service.ReceiptService;
import com.qoocca.teachers.auth.security.ParentUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Parent Receipt API", description = "보호자 결제 요청 목록 조회")
@RestController
@RequestMapping("/api/parent/receipt")
@RequiredArgsConstructor
public class ParentReceiptController {

    private final ReceiptService receiptService;

    @Operation(summary = "결제 요청 건 조회")
    @GetMapping("/requests")
    public ResponseEntity<List<ParentReceiptResponse>> getPaymentRequests(
            @Parameter(hidden = true) @AuthenticationPrincipal ParentUserDetails parentUserDetails) {
        return ResponseEntity.ok(receiptService.getPendingReceiptsByParent(parentUserDetails.getParentId()));
    }

    @Operation(summary = "결제 요청 상세 조회")
    @GetMapping("/{receiptId}")
    public ResponseEntity<ParentReceiptResponse> getPaymentRequestDetail(
            @PathVariable Long receiptId,
            @Parameter(hidden = true) @AuthenticationPrincipal ParentUserDetails parentUserDetails) {
        return ResponseEntity.ok(receiptService.getReceiptDetailForParent(receiptId, parentUserDetails.getParentId()));
    }
}
