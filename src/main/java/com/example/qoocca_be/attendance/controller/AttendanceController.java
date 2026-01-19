package com.example.qoocca_be.attendance.controller;

import com.example.qoocca_be.attendance.model.AttendanceCreateRequest;
import com.example.qoocca_be.attendance.model.AttendanceMonthResponse;
import com.example.qoocca_be.attendance.model.AttendanceResponse;
import com.example.qoocca_be.attendance.model.StudentCalendarResponse;
import com.example.qoocca_be.attendance.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /* =========================
     * 출결 등록
     * POST /api/student/{studentId}/attendance
     * ========================= */
    @PostMapping("/api/student/{studentId}/attendance")
    public ResponseEntity<AttendanceResponse> createAttendance(
            @PathVariable Long studentId,
            @Valid @RequestBody AttendanceCreateRequest request
    ) {
        AttendanceResponse response = attendanceService.createAttendance(studentId, request);
        return ResponseEntity.ok(response);
    }

    /* =========================
     * 단일 날짜 조회
     * GET /api/student/{studentId}/attendance?date=2026-01-05
     * ========================= */
    @GetMapping("/api/student/{studentId}/attendance")
    public ResponseEntity<AttendanceResponse> getAttendanceByDate(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AttendanceResponse response = attendanceService.getAttendanceByDate(studentId, date);
        return ResponseEntity.ok(response);
    }

    /* =========================
     * 한 달 조회
     * GET /api/student/{studentId}/attendance/month?year=2026&month=1
     * ========================= */
    @GetMapping("/api/attendance/{studentId}/calendar-view")
    public ResponseEntity<StudentCalendarResponse> getCalendarView(
            @PathVariable Long studentId,
            @RequestParam Long academyId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(attendanceService.getStudentCalendarView(studentId, academyId, year, month));
    }

    /* =========================
     * 하교(체크아웃) 등록
     * PATCH /api/student/{studentId}/attendance/check-out
     * ========================= */
    @PatchMapping("/api/student/{studentId}/attendance/check-out")
    public ResponseEntity<AttendanceResponse> checkOut(
            @PathVariable Long studentId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        AttendanceResponse response = attendanceService.updateCheckOut(studentId, date);
        return ResponseEntity.ok(response);
    }
}
