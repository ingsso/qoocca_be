package com.qoocca.teachers.api.attendance.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceCheckOutRequest {

    @NotNull
    @Schema(
            description = "출결 날짜",
            example = "2026-02-04",
            type = "string"
    )
    private LocalDate attendanceDate;

    @NotNull
    @Schema(
            description = "하원 시간 (HH:mm)",
            example = "17:30",
            type = "string"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime checkOut;
}
