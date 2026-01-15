package com.example.qoocca_be.receipt.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DashboardMainSummaryResponse {
    private String className;
    private String classTime;
    private String status;
    private String statusLabel;
    private Long totalAmount;
}