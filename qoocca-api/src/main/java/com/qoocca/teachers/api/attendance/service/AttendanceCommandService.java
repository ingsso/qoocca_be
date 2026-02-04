package com.qoocca.teachers.api.attendance.service;

import com.qoocca.teachers.api.attendance.model.AttendanceCheckOutRequest;
import com.qoocca.teachers.api.attendance.model.AttendanceCreateRequest;
import com.qoocca.teachers.api.attendance.model.AttendanceResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.attendance.entity.AttendanceEntity;
import com.qoocca.teachers.db.attendance.repository.AttendanceRepository;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceCommandService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;

    public AttendanceResponse createAttendance(Long studentId, AttendanceCreateRequest request) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        ClassInfoEntity targetClass = findMatchingClass(studentId, request.getAttendanceDate(), request.getCheckIn());

        if (attendanceRepository.existsByStudent_StudentIdAndClassInfo_ClassIdAndAttendanceDate(
                studentId, targetClass.getClassId(), request.getAttendanceDate())) {
            throw new CustomException(ErrorCode.ATTENDANCE_ALREADY_EXISTS);
        }

        AttendanceEntity attendance = AttendanceEntity.builder()
                .student(student)
                .classInfo(targetClass)
                .attendanceDate(request.getAttendanceDate())
                .checkIn(request.getCheckIn())
                .build();

        attendance.calculateStatus(targetClass.getStartTime());
        AttendanceEntity saved = attendanceRepository.save(attendance);
        return AttendanceResponse.fromEntity(saved);
    }

    public AttendanceResponse updateCheckOut(Long studentId, AttendanceCheckOutRequest request) {
        AttendanceEntity attendance = attendanceRepository
            .findFirstByStudent_StudentIdAndAttendanceDateAndCheckOutIsNullOrderByCheckInDesc(
                    studentId, request.getAttendanceDate()
            ).orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));

        attendance.setCheckOut(request.getCheckOut());

        // 조퇴 판단
        if (request.getCheckOut().isBefore(attendance.getClassInfo().getEndTime())) {
            attendance.setStatus(AttendanceEntity.AttendanceStatus.EARLY_LEAVE);
        }

        return AttendanceResponse.fromEntity(attendance);
    }

    private ClassInfoEntity findMatchingClass(Long studentId, LocalDate attendanceDate, LocalTime checkIn) {
        List<ClassInfoEntity> classes = classInfoStudentRepository.findClassesByStudentId(studentId, StudentStatus.ENROLLED);
        String dayOfWeek = attendanceDate.getDayOfWeek().name().toLowerCase();

        return classes.stream()
                .filter(c -> isClassOnDay(c, dayOfWeek))
                .filter(c -> isTimeWithinRange(c, checkIn))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND_FOR_TIME));
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
}