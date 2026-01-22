package com.qoocca.teachers.attendance.model;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentMonthlyStatResponse {
    private Long studentId;
    private String studentName;
    private long presentCount; // 출석 횟수
    private long lateCount;    // 지각 횟수
    private long absentCount;  // 결석 횟수
}