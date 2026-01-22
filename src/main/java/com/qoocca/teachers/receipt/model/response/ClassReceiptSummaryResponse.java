package com.qoocca.teachers.receipt.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ClassReceiptSummaryResponse {
    private String className;
    private String classTime;
    private String receiptStatus;
    private Long totalAmount;
}
