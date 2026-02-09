package com.qoocca.teachers.db.attendance.model;

public interface StudentMonthlyStatProjection {
    Long getStudentId();
    String getStudentName();
    Long getPresentCount();
    Long getLateCount();
    Long getAbsentCount();
}
