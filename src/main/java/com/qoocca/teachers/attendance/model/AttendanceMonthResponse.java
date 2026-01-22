package com.qoocca.teachers.attendance.model;

import com.qoocca.teachers.attendance.entity.AttendanceEntity;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
public class AttendanceMonthResponse {
    private LocalDate date;
    private Long classId;
    private String className;
    private String status;
    private String statusLabel;
    private LocalTime checkIn;
    private LocalTime checkOut;

    public static AttendanceMonthResponse fromEntity(AttendanceEntity entity) {
        return AttendanceMonthResponse.builder()
                .date(entity.getAttendanceDate())
                .classId(entity.getClassInfo().getClassId())
                .className(entity.getClassInfo().getClassName())
                .status(entity.getStatus().name())
                .statusLabel(formatStatusLabel(entity.getStatus()))
                .checkIn(entity.getCheckIn())
                .checkOut(entity.getCheckOut())
                .build();
    }

    private static String formatStatusLabel(AttendanceEntity.AttendanceStatus status) {
        return switch (status) {
            case PRESENT -> "출석";
            case LATE -> "지각";
            case ABSENT -> "결석";
            case EARLY_LEAVE -> "조퇴";
        };
    }
}
