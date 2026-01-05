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
public class AttendanceMonthResponse {

    private LocalDate attendanceDate;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private AttendanceStatus status;

    public static AttendanceMonthResponse fromEntity(AttendanceEntity entity) {
        return AttendanceMonthResponse.builder()
                .attendanceDate(entity.getAttendanceDate())
                .checkIn(entity.getCheckIn())
                .checkOut(entity.getCheckOut())
                .status(entity.getStatus())
                .build();
    }
}
