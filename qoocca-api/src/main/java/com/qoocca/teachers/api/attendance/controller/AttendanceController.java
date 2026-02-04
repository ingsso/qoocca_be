package com.qoocca.teachers.api.attendance.controller;

import com.qoocca.teachers.api.attendance.model.AttendanceCheckOutRequest;
import com.qoocca.teachers.api.attendance.model.AttendanceCreateRequest;
import com.qoocca.teachers.api.attendance.model.AttendanceResponse;
import com.qoocca.teachers.api.attendance.model.StudentCalendarResponse;
import com.qoocca.teachers.api.attendance.service.AttendanceCommandService;
import com.qoocca.teachers.api.attendance.service.AttendanceQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "Attendance API", description = "학생 출결 등록 및 조회 API")
@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceCommandService attendanceCommandService;
    private final AttendanceQueryService attendanceQueryService;

    @Operation(summary = "출결(등원) 등록", description = "학생의 등원 시간을 기록하고 출결 상태를 계산합니다.")
    @PostMapping("/api/student/{studentId}/attendance")
    public ResponseEntity<AttendanceResponse> createAttendance(
            @Parameter(description = "학생 ID", example = "1") @PathVariable Long studentId,
            @Valid @RequestBody AttendanceCreateRequest request
    ) {
        AttendanceResponse response = attendanceCommandService.createAttendance(studentId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일자별 출결 조회", description = "특정 날짜의 학생 출결 기록을 조회합니다.")
    @GetMapping("/api/student/{studentId}/attendance")
    public ResponseEntity<AttendanceResponse> getAttendanceByDate(
            @Parameter(description = "학생 ID", example = "1") @PathVariable Long studentId,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", example = "2026-01-19")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AttendanceResponse response = attendanceQueryService.getAttendanceByDate(studentId, date);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "월간 출결 캘린더 조회", description = "학생의 월간 출결 정보를 캘린더 형태로 조회합니다.")
    @GetMapping("/api/attendance/{studentId}/calendar-view")
    public ResponseEntity<StudentCalendarResponse> getCalendarView(
            @Parameter(description = "학생 ID", example = "1") @PathVariable Long studentId,
            @Parameter(description = "학원 ID", example = "1") @RequestParam Long academyId,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam int year,
            @Parameter(description = "조회 월", example = "1") @RequestParam int month) {
        return ResponseEntity.ok(attendanceQueryService.getStudentCalendarView(studentId, academyId, year, month));
    }

    @Operation(summary = "하원 처리", description = "기존 등원 기록에 하원 시간을 반영합니다.")
    @PatchMapping("/api/student/{studentId}/attendance/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(
            @Parameter(description = "학생 ID", example = "1") @PathVariable Long studentId,
            @Valid @RequestBody AttendanceCheckOutRequest request
    ) {
        AttendanceResponse response = attendanceCommandService.updateCheckOut(studentId, request);
        return ResponseEntity.ok(response);
    }
}