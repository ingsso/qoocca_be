package com.qoocca.teachers.api.receipt.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DashboardMainSummaryResponse {
    private String className;
    private String classTime;
    private String status;
    private String statusLabel;
    private Long totalAmount;
}
