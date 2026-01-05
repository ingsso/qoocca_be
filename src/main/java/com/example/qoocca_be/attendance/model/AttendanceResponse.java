package com.example.qoocca_be.attendance.model;

import com.example.qoocca_be.attendance.entity.AttendanceEntity;
import com.example.qoocca_be.attendance.entity.AttendanceEntity.AttendanceStatus;
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
    private LocalDate attendanceDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private AttendanceStatus status;

    public static AttendanceResponse fromEntity(AttendanceEntity entity) {
        return AttendanceResponse.builder()
                .attendanceId(entity.getAttendanceId())
                .studentId(entity.getStudent().getStudentId())
                .attendanceDate(entity.getAttendanceDate())
                .checkIn(entity.getCheckIn())
                .checkOut(entity.getCheckOut())
                .status(entity.getStatus())
                .build();
    }
}
