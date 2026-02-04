package com.qoocca.teachers.api.attendance.model;

import com.qoocca.teachers.db.attendance.entity.AttendanceEntity.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "출결(등원) 생성 요청 DTO")
public class AttendanceCreateRequest {

    @NotNull
    @Schema(description = "출결 날짜", example = "2026-02-04")
    private LocalDate attendanceDate;

    @NotNull
    @Schema(description = "등원 시간", type = "string", example = "09:05:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    private LocalTime checkIn;
}
