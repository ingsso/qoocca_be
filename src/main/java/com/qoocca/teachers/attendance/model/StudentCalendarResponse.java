package com.qoocca.teachers.attendance.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentCalendarResponse {
    private String studentName;
    private List<String> enrolledClasses;
    private List<AttendanceResponse> attendanceRecords;
}
