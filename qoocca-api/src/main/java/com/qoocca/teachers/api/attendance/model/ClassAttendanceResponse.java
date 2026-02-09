package com.qoocca.teachers.api.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassAttendanceResponse {
    private String className;
    private Long studentId;
    private String studentName;
    private LocalTime checkIn;    // 등원 시간
    private LocalTime checkOut;   // 하원 시간
    private String status;        // PRESENT, LATE, ABSENT 등
    private String statusLabel;   // "등원 13:00", "미등원" 등 포맷팅된 라벨
}
