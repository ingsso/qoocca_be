package com.example.qoocca_be.academy.model.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DashboardStatsResponse {
    private Long studentCount;      // 전체 원생 수
    private Long presentCount;      // 현재 실제 등원 완료 수
    private Long totalTodayCount;   // 오늘 등원해야 하는 총 학생 수
    private Long noCardCount;       // 카드 미등록 학생 수
    private Long totalMonthlyFee;   // 이번 달 총 수납액
}