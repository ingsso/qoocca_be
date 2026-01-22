package com.qoocca.teachers.attendance.model;

import com.qoocca.teachers.attendance.entity.AttendanceEntity;
import com.qoocca.teachers.attendance.entity.AttendanceEntity.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttendanceResponse {

    private Long attendanceId;
    private Long studentId;
    private String studentName;
    private Long classId;
    private String className;
    private LocalDate attendanceDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private AttendanceStatus status;
    private String statusLabel;

    public static AttendanceResponse fromEntity(AttendanceEntity entity) {
        return AttendanceResponse.builder()
                .attendanceId(entity.getAttendanceId())
                .studentId(entity.getStudent().getStudentId())
                .studentName(entity.getStudent().getStudentName())
                .classId(entity.getClassInfo().getClassId())
                .className(entity.getClassInfo().getClassName())
                .attendanceDate(entity.getAttendanceDate())
                .checkIn(entity.getCheckIn())
                .checkOut(entity.getCheckOut())
                .status(entity.getStatus())
                .statusLabel(formatStatusLabel(entity.getStatus())) //
                .build();
    }

    private static String formatStatusLabel(AttendanceStatus status) { //
        return switch (status) {
            case PRESENT -> "출석";
            case LATE -> "지각";
            case ABSENT -> "결석";
            case EARLY_LEAVE -> "조퇴";
        };
    }
}
