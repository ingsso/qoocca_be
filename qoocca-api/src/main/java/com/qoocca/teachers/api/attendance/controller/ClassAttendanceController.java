package com.qoocca.teachers.api.attendance.controller;

import com.qoocca.teachers.api.attendance.model.ClassAttendanceResponse;
import com.qoocca.teachers.api.attendance.model.StudentMonthlyStatResponse;
import com.qoocca.teachers.api.attendance.service.AttendanceAnalyticsService;
import com.qoocca.teachers.api.classInfo.model.response.ClassSummaryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Class Attendance API", description = "반별 출결 관리 및 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class ClassAttendanceController {

    private final AttendanceAnalyticsService attendanceAnalyticsService;

    @Operation(summary = "학원별 오늘 출결 현황 조회", description = "학원 기준으로 오늘 출결 상세를 조회합니다.")
    @GetMapping("/academy/{academyId}/today")
    public ResponseEntity<List<ClassAttendanceResponse>> getTodayClassAttendance(
            @Parameter(description = "학원 ID") @PathVariable Long academyId) {
        return ResponseEntity.ok(attendanceAnalyticsService.getTodayAttendanceByAcademy(academyId));
    }

    @Operation(summary = "학원별 반 출결 요약 조회", description = "특정 날짜 기준 반별 출결 요약을 조회합니다.")
    @GetMapping("/academy/{academyId}/summary")
    public ResponseEntity<List<ClassSummaryResponse>> getTodayClassSummaries(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", example = "2026-01-19")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(attendanceAnalyticsService.getClassSummariesByDate(academyId, targetDate));
    }

    @Operation(summary = "반 월간 출결 통계 조회", description = "반별 학생 월간 출결 통계를 조회합니다.")
    @GetMapping("/class/{classId}/monthly-stats")
    public ResponseEntity<List<StudentMonthlyStatResponse>> getMonthlyStats(
            @Parameter(description = "반 ID") @PathVariable Long classId,
            @Parameter(description = "조회 연도") @RequestParam int year,
            @Parameter(description = "조회 월") @RequestParam int month) {
        return ResponseEntity.ok(attendanceAnalyticsService.getMonthlyStatsByClass(classId, year, month));
    }
}