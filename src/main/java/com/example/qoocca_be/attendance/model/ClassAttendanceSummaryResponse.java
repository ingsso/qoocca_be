package com.example.qoocca_be.attendance.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class ClassAttendanceSummaryResponse {
    private Long classId;
    private String className;
    private String classTime;
    private int totalEnrolled;
    private int presentCount;
    private int lateCount;
    private int absentCount;
    private int notPresentCount;
}