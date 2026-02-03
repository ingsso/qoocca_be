package com.qoocca.teachers.api.attendance.service;

import com.qoocca.teachers.api.attendance.model.AttendanceCreateRequest;
import com.qoocca.teachers.api.attendance.model.AttendanceResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.attendance.entity.AttendanceEntity;
import com.qoocca.teachers.db.attendance.repository.AttendanceRepository;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
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
    @Mock
    private ClassInfoRepository classInfoRepository;

    @InjectMocks
    private AttendanceService attendanceService;

    @Test
    void createAttendanceUsesRequestAttendanceDateForDayMatching() {
        Long studentId = 1L;
        LocalDate attendanceDate = LocalDate.now().plusDays(1);
        LocalTime checkIn = LocalTime.of(10, 0);

        StudentEntity student = StudentEntity.builder()
                .studentId(studentId)
                .studentName("student")
                .build();
        ClassInfoEntity classInfo = classForDate(attendanceDate);

        when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
        when(classInfoStudentRepository.findClassesByStudentId(studentId, StudentStatus.ENROLLED))
                .thenReturn(List.of(classInfo));
        when(attendanceRepository.existsByStudent_StudentIdAndClassInfo_ClassIdAndAttendanceDate(
                studentId, classInfo.getClassId(), attendanceDate
        )).thenReturn(false);
        when(attendanceRepository.save(any(AttendanceEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceCreateRequest request = AttendanceCreateRequest.builder()
                .attendanceDate(attendanceDate)
                .checkIn(checkIn)
                .status(AttendanceEntity.AttendanceStatus.PRESENT)
                .build();

        AttendanceResponse response = attendanceService.createAttendance(studentId, request);

        assertEquals(studentId, response.getStudentId());
        assertEquals(classInfo.getClassId(), response.getClassId());
        assertEquals(AttendanceEntity.AttendanceStatus.PRESENT, response.getStatus());
    }

    @Test
    void updateCheckOutPrefersOpenAttendanceRecord() {
        Long studentId = 2L;
        LocalDate date = LocalDate.now();

        AttendanceEntity openAttendance = AttendanceEntity.builder()
                .attendanceId(10L)
                .student(StudentEntity.builder().studentId(studentId).studentName("s").build())
                .classInfo(ClassInfoEntity.builder().classId(20L).className("c").endTime(LocalTime.MAX).build())
                .attendanceDate(date)
                .checkIn(LocalTime.of(9, 0))
                .status(AttendanceEntity.AttendanceStatus.PRESENT)
                .build();

        when(attendanceRepository.findFirstByStudent_StudentIdAndAttendanceDateAndCheckOutIsNullOrderByCheckInDesc(studentId, date))
                .thenReturn(Optional.of(openAttendance));

        AttendanceResponse response = attendanceService.updateCheckOut(studentId, date);

        verify(attendanceRepository, never()).findByStudent_StudentIdAndAttendanceDate(studentId, date);
        assertNotNull(response.getCheckOut());
        assertEquals(AttendanceEntity.AttendanceStatus.EARLY_LEAVE, response.getStatus());
    }

    @Test
    void updateCheckOutThrowsWhenNoAttendanceRecordExists() {
        Long studentId = 3L;
        LocalDate date = LocalDate.now();

        when(attendanceRepository.findFirstByStudent_StudentIdAndAttendanceDateAndCheckOutIsNullOrderByCheckInDesc(studentId, date))
                .thenReturn(Optional.empty());
        when(attendanceRepository.findByStudent_StudentIdAndAttendanceDate(studentId, date))
                .thenReturn(Optional.empty());

        CustomException exception = assertThrows(CustomException.class, () -> attendanceService.updateCheckOut(studentId, date));
        assertEquals(ErrorCode.ATTENDANCE_NOT_FOUND, exception.getErrorCode());
    }

    private ClassInfoEntity classForDate(LocalDate date) {
        String day = date.getDayOfWeek().name().toLowerCase();
        return ClassInfoEntity.builder()
                .classId(100L)
                .className("class")
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .monday("monday".equals(day))
                .tuesday("tuesday".equals(day))
                .wednesday("wednesday".equals(day))
                .thursday("thursday".equals(day))
                .friday("friday".equals(day))
                .saturday("saturday".equals(day))
                .sunday("sunday".equals(day))
                .build();
    }
}
