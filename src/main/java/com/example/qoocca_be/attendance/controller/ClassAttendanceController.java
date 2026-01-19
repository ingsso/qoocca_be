package com.example.qoocca_be.attendance.controller;

import com.example.qoocca_be.attendance.model.ClassAttendanceResponse;
import com.example.qoocca_be.attendance.model.StudentMonthlyStatResponse;
import com.example.qoocca_be.attendance.service.AttendanceService;
import com.example.qoocca_be.classInfo.model.ClassSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/attendance")
public class ClassAttendanceController {
    private final AttendanceService attendanceService;

    @GetMapping("/academy/{academyId}/today")
    public ResponseEntity<List<ClassAttendanceResponse>> getTodayClassAttendance(@PathVariable Long academyId) {
        return ResponseEntity.ok(attendanceService.getTodayAttendanceByAcademy(academyId));
    }

    @GetMapping("/academy/{academyId}/summary")
    public ResponseEntity<List<ClassSummaryResponse>> getTodayClassSummaries(
            @PathVariable Long academyId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        LocalDate targetDate = (date != null) ? date : LocalDate.now();
        return ResponseEntity.ok(attendanceService.getClassSummariesByDate(academyId, targetDate));
    }

    @GetMapping("/class/{classId}/monthly-stats")
    public ResponseEntity<List<StudentMonthlyStatResponse>> getMonthlyStats(
            @PathVariable Long classId,
            @RequestParam int year,
            @RequestParam int month) {
        return ResponseEntity.ok(attendanceService.getMonthlyStatsByClass(classId, year, month));
    }
}
