package com.example.qoocca_be.attendance.model;

import com.example.qoocca_be.attendance.entity.AttendanceEntity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
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
public class AttendanceCreateRequest {

    @NotNull
    private LocalDate attendanceDate;

    private LocalTime checkIn;
    private LocalTime checkOut;

    @NotNull
    private AttendanceStatus status;
}
