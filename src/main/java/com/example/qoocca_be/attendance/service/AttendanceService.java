package com.example.qoocca_be.attendance.service;

import com.example.qoocca_be.attendance.entity.AttendanceEntity;
import com.example.qoocca_be.attendance.model.AttendanceCreateRequest;
import com.example.qoocca_be.attendance.model.AttendanceMonthResponse;
import com.example.qoocca_be.attendance.model.AttendanceResponse;
import com.example.qoocca_be.attendance.repository.AttendanceRepository;
import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.classInfo.repository.ClassInfoStudentRepository;
import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;

    /* =========================
     * 출결 등록
     * ========================= */
    public AttendanceResponse createAttendance(Long studentId, AttendanceCreateRequest request) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        ClassInfoEntity targetClass = findMatchingClass(studentId, request.getCheckIn());

        AttendanceEntity attendance = AttendanceEntity.builder()
                .student(student)
                .classInfo(targetClass)
                .attendanceDate(request.getAttendanceDate())
                .checkIn(request.getCheckIn())
                .checkOut(request.getCheckOut())
                .status(request.getStatus())
                .build();

        AttendanceEntity saved = attendanceRepository.save(attendance);

        return AttendanceResponse.fromEntity(saved);
    }

    private ClassInfoEntity findMatchingClass(Long studentId, LocalTime checkIn) {
        List<ClassInfoEntity> classes = classInfoStudentRepository.findClassesByStudentId(studentId, StudentStatus.ENROLLED);

        String dayOfWeek = LocalDate.now().getDayOfWeek().name().toLowerCase();

        return classes.stream()
                .filter(c -> isClassOnDay(c, dayOfWeek))
                .filter(c -> isTimeWithinRange(c, checkIn))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("현재 시간에 해당 학생이 수강하는 수업이 없습니다."));
    }

    private boolean isClassOnDay(ClassInfoEntity c, String day) {
        return switch (day) {
            case "monday" -> c.isMonday();
            case "tuesday" -> c.isTuesday();
            case "wednesday" -> c.isWednesday();
            case "thursday" -> c.isThursday();
            case "friday" -> c.isFriday();
            case "saturday" -> c.isSaturday();
            case "sunday" -> c.isSunday();
            default -> false;
        };
    }

    private boolean isTimeWithinRange(ClassInfoEntity c, LocalTime checkInTime) {
        return !checkInTime.isBefore(c.getStartTime().minusMinutes(30)) &&
                !checkInTime.isAfter(c.getEndTime());
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
