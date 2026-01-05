package com.example.qoocca_be.attendance.service;

import com.example.qoocca_be.attendance.entity.AttendanceEntity;
import com.example.qoocca_be.attendance.model.AttendanceCreateRequest;
import com.example.qoocca_be.attendance.model.AttendanceMonthResponse;
import com.example.qoocca_be.attendance.model.AttendanceResponse;
import com.example.qoocca_be.attendance.repository.AttendanceRepository;
import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;

    /* =========================
     * 출결 등록
     * ========================= */
    public AttendanceResponse createAttendance(Long studentId, AttendanceCreateRequest request) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        AttendanceEntity attendance = AttendanceEntity.builder()
                .student(student)
                .attendanceDate(request.getAttendanceDate())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .status(request.getStatus())
                .build();

        AttendanceEntity saved = attendanceRepository.save(attendance);

        return AttendanceResponse.fromEntity(saved);
    }

    /* =========================
     * 단일 날짜 조회
     * ========================= */
    public AttendanceResponse getAttendanceByDate(Long studentId, LocalDate date) {
        AttendanceEntity attendance = attendanceRepository
                .findByStudent_StudentIdAndAttendanceDate(studentId, date)
                .orElseThrow(() -> new IllegalArgumentException("해당 날짜 출결을 찾을 수 없습니다."));

        return AttendanceResponse.fromEntity(attendance);
    }

    /* =========================
     * 한 달 조회
     * ========================= */
    public List<AttendanceMonthResponse> getAttendanceByMonth(Long studentId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AttendanceEntity> list = attendanceRepository
                .findByStudent_StudentIdAndAttendanceDateBetween(studentId, startDate, endDate);

        return list.stream()
                .map(AttendanceMonthResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
