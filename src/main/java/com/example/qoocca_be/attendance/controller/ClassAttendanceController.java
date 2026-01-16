package com.example.qoocca_be.attendance.controller;

import com.example.qoocca_be.attendance.model.ClassAttendanceResponse;
import com.example.qoocca_be.attendance.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

}
