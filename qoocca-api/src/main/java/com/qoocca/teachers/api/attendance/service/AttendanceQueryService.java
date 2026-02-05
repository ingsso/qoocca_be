package com.qoocca.teachers.api.attendance.service;

import com.qoocca.teachers.api.attendance.model.AttendanceResponse;
import com.qoocca.teachers.api.attendance.model.StudentCalendarResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.attendance.entity.AttendanceEntity;
import com.qoocca.teachers.db.attendance.repository.AttendanceRepository;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceQueryService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;

    public AttendanceResponse getAttendanceByDate(Long studentId, LocalDate date) {
        AttendanceEntity attendance = attendanceRepository
                .findFirstByStudent_StudentIdAndAttendanceDateOrderByCheckInDesc(studentId, date)
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));

        return AttendanceResponse.fromEntity(attendance);
    }

    public StudentCalendarResponse getStudentCalendarView(Long studentId, Long academyId, int year, int month) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        List<ClassInfoStudentEntity> enrollments = classInfoStudentRepository
                .findAllByAcademyAndStatus(academyId, StudentStatus.ENROLLED);

        List<String> enrolledClassNames = enrollments.stream()
                .filter(e -> e.getStudent().getStudentId().equals(studentId))
                .map(e -> e.getClassInfo().getClassName())
                .toList();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<AttendanceEntity> attendanceEntities = attendanceRepository
                .findByStudentAndAcademyAndDateBetween(studentId, academyId, startDate, endDate);

        List<AttendanceResponse> records = attendanceEntities.stream()
                .map(AttendanceResponse::fromEntity)
                .toList();

        return StudentCalendarResponse.builder()
                .studentName(student.getStudentName())
                .enrolledClasses(enrolledClassNames)
                .attendanceRecords(records)
                .build();
    }
}
