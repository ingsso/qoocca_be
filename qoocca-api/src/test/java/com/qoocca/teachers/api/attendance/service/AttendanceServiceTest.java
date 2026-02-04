package com.qoocca.teachers.api.attendance.service;

import com.qoocca.teachers.api.attendance.model.AttendanceCreateRequest;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.attendance.entity.AttendanceEntity;
import com.qoocca.teachers.db.attendance.repository.AttendanceRepository;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;
    @Mock
    private StudentRepository studentRepository;
    @Mock
    private ClassInfoStudentRepository classInfoStudentRepository;
    @InjectMocks
    private AttendanceCommandService attendanceCommandService;

    @Test
    void createAttendanceUsesRequestAttendanceDateForDayMatching() {
        Long studentId = 1L;
        LocalDate attendanceDate = LocalDate.of(2026, 2, 3); // Tuesday
        LocalTime checkIn = LocalTime.of(10, 0);

        StudentEntity student = buildStudent(studentId, "학생A");
        ClassInfoEntity mondayClass = buildClass(10L, "월요반", true, false, LocalTime.of(9, 0), LocalTime.of(12, 0));
        ClassInfoEntity tuesdayClass = buildClass(20L, "화요반", false, true, LocalTime.of(9, 0), LocalTime.of(12, 0));

        AttendanceCreateRequest request = AttendanceCreateRequest.builder()
                .attendanceDate(attendanceDate)
                .checkIn(checkIn)
                .status(AttendanceEntity.AttendanceStatus.PRESENT)
                .build();

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(classInfoStudentRepository.findClassesByStudentId(studentId, StudentStatus.ENROLLED))
                .thenReturn(List.of(mondayClass, tuesdayClass));
        when(attendanceRepository.existsByStudent_StudentIdAndClassInfo_ClassIdAndAttendanceDate(studentId, 20L, attendanceDate))
                .thenReturn(false);
        when(attendanceRepository.save(any(AttendanceEntity.class))).thenAnswer(invocation -> {
            AttendanceEntity source = invocation.getArgument(0);
            return AttendanceEntity.builder()
                    .attendanceId(100L)
                    .student(source.getStudent())
                    .classInfo(source.getClassInfo())
                    .attendanceDate(source.getAttendanceDate())
                    .checkIn(source.getCheckIn())
                    .checkOut(source.getCheckOut())
                    .status(source.getStatus())
                    .build();
        });

        var response = attendanceCommandService.createAttendance(studentId, request);

        verify(attendanceRepository).existsByStudent_StudentIdAndClassInfo_ClassIdAndAttendanceDate(studentId, 20L, attendanceDate);
        assertEquals(20L, response.getClassId());
        assertEquals(attendanceDate, response.getAttendanceDate());
    }

    @Test
    void updateCheckOutPrefersOpenAttendanceRecord() {
        Long studentId = 1L;
        LocalDate attendanceDate = LocalDate.of(2026, 2, 3);
        StudentEntity student = buildStudent(studentId, "학생A");
        ClassInfoEntity classInfo = buildClass(20L, "화요반", false, true, LocalTime.of(9, 0), LocalTime.of(18, 0));

        AttendanceEntity openAttendance = AttendanceEntity.builder()
                .attendanceId(111L)
                .student(student)
                .classInfo(classInfo)
                .attendanceDate(attendanceDate)
                .checkIn(LocalTime.of(10, 0))
                .status(AttendanceEntity.AttendanceStatus.PRESENT)
                .build();

        when(attendanceRepository.findFirstByStudent_StudentIdAndAttendanceDateAndCheckOutIsNullOrderByCheckInDesc(studentId, attendanceDate))
                .thenReturn(Optional.of(openAttendance));

        var response = attendanceCommandService.updateCheckOut(studentId, attendanceDate);

        verify(attendanceRepository, never()).findByStudent_StudentIdAndAttendanceDate(studentId, attendanceDate);
        assertEquals(111L, response.getAttendanceId());
        assertNotNull(response.getCheckOut());
    }

    @Test
    void updateCheckOutThrowsWhenNoAttendanceRecordExists() {
        Long studentId = 1L;
        LocalDate attendanceDate = LocalDate.of(2026, 2, 3);

        when(attendanceRepository.findFirstByStudent_StudentIdAndAttendanceDateAndCheckOutIsNullOrderByCheckInDesc(studentId, attendanceDate))
                .thenReturn(Optional.empty());
        when(attendanceRepository.findByStudent_StudentIdAndAttendanceDate(studentId, attendanceDate))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class,
                () -> attendanceCommandService.updateCheckOut(studentId, attendanceDate));

        assertEquals(ErrorCode.ATTENDANCE_NOT_FOUND, exception.getErrorCode());
    }

    private StudentEntity buildStudent(Long studentId, String studentName) {
        return StudentEntity.builder()
                .studentId(studentId)
                .studentName(studentName)
                .build();
    }

    private ClassInfoEntity buildClass(Long classId, String className, boolean monday, boolean tuesday,
                                       LocalTime startTime, LocalTime endTime) {
        return ClassInfoEntity.builder()
                .classId(classId)
                .className(className)
                .startTime(startTime)
                .endTime(endTime)
                .monday(monday)
                .tuesday(tuesday)
                .build();
    }
}
