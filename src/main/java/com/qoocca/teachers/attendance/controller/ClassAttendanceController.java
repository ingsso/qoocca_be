package com.qoocca.teachers.attendance.controller;

import com.qoocca.teachers.attendance.model.ClassAttendanceResponse;
import com.qoocca.teachers.attendance.model.StudentMonthlyStatResponse;
import com.qoocca.teachers.attendance.service.AttendanceService;
import com.qoocca.teachers.classInfo.model.response.ClassSummaryResponse;
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
    private final AttendanceService attendanceService;

    @Operation(summary = "학원별 오늘 출결 현황 상세 조회", description = "학원 내 오늘 수업이 있는 모든 학생의 출결 상태와 등/하원 시간을 조회합니다.")
    @GetMapping("/academy/{academyId}/today")
    public ResponseEntity<List<ClassAttendanceResponse>> getTodayClassAttendance(
            @Parameter(description = "학원 ID") @PathVariable Long academyId) {
        return ResponseEntity.ok(attendanceService.getTodayAttendanceByAcademy(academyId));
    }

    @Operation(summary = "학원별 클래스 출결 요약 통계 조회", description = "특정 날짜의 클래스별 출석, 지각, 결석 인원 수 요약을 조회합니다. 날짜 미입력 시 오늘 날짜를 기준으로 합니다.")
    @GetMapping("/academy/{academyId}/summary")
    public ResponseEntity<List<ClassSummaryResponse>> getTodayClassSummaries(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)", example = "2026-01-19")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(attendanceService.getClassSummariesByDate(academyId, targetDate));
    }

    @Operation(summary = "클래스별 월간 출결 통계 조회", description = "특정 클래스의 학생별 한 달간 출석, 지각, 결석 횟수 합계를 조회합니다.")
    @GetMapping("/class/{classId}/monthly-stats")
    public ResponseEntity<List<StudentMonthlyStatResponse>> getMonthlyStats(
            @Parameter(description = "클래스 ID") @PathVariable Long classId,
            @Parameter(description = "조회 연도") @RequestParam int year,
            @Parameter(description = "조회 월") @RequestParam int month) {
        return ResponseEntity.ok(attendanceService.getMonthlyStatsByClass(classId, year, month));
    }
}
