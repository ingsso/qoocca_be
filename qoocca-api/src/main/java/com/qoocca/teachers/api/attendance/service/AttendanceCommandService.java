package com.qoocca.teachers.api.attendance.service;

import com.qoocca.teachers.api.attendance.model.AttendanceCreateRequest;
import com.qoocca.teachers.api.attendance.model.AttendanceCheckOutRequest;
import com.qoocca.teachers.api.attendance.model.AttendanceResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.attendance.entity.AttendanceEntity;
import com.qoocca.teachers.db.attendance.repository.AttendanceRepository;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AttendanceCommandService {

    private final AttendanceRepository attendanceRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;
    private final AttendanceCacheService attendanceCacheService;

    public AttendanceResponse createAttendance(Long studentId, AttendanceCreateRequest request) {
        // [최적화] 트랜잭션 밖에서 Fetch Join으로 학생/수업/학원 정보를 한 번에 가져옴
        ClassInfoStudentEntity enrollment = findMatchingEnrollment(
                studentId,
                request.getAttendanceDate(),
                request.getCheckIn()
        );

        return saveAttendanceProcess(enrollment, request);
    }

    @Transactional
    protected AttendanceResponse saveAttendanceProcess(ClassInfoStudentEntity enrollment, AttendanceCreateRequest request) {
        try {
            // 1. 객체 생성 (Fetch Join으로 이미 메모리에 로드된 상태)
            AttendanceEntity attendance = AttendanceEntity.builder()
                    .student(enrollment.getStudent())
                    .classInfo(enrollment.getClassInfo())
                    .attendanceDate(request.getAttendanceDate())
                    .checkIn(request.getCheckIn())
                    .build();

            attendance.calculateStatus(enrollment.getClassInfo().getStartTime());

            // 2. save로 변경 (flush 생략하여 오버헤드 감소)
            AttendanceEntity saved = attendanceRepository.save(attendance);

            // 3. 캐시 삭제 (반드시 @Async가 제거된 상태여야 함)
            attendanceCacheService.evictAttendanceCaches(
                    enrollment.getClassInfo().getAcademy().getId(),
                    request.getAttendanceDate()
            );

            return AttendanceResponse.fromEntity(saved);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.ATTENDANCE_ALREADY_EXISTS);
        }
    }

    private ClassInfoStudentEntity findMatchingEnrollment(Long studentId, LocalDate attendanceDate, LocalTime checkIn) {
        String dayOfWeek = attendanceDate.getDayOfWeek().name().toLowerCase();

        // [핵심] JOIN FETCH가 포함된 쿼리 호출
        List<ClassInfoStudentEntity> enrollments = classInfoStudentRepository.findEnrollmentsWithDetails(
                studentId,
                StudentStatus.ENROLLED,
                dayOfWeek
        );

        return enrollments.stream()
                .filter(e -> {
                    ClassInfoEntity c = e.getClassInfo();
                    return !checkIn.isBefore(c.getStartTime().minusMinutes(30)) && !checkIn.isAfter(c.getEndTime());
                })
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND_FOR_TIME));
    }

    @Transactional
    public AttendanceResponse updateCheckOut(Long studentId, AttendanceCheckOutRequest request) {
        // DB 복합 인덱스를 활용한 최적화 조회
        AttendanceEntity attendance = attendanceRepository
                .findFirstByStudent_StudentIdAndAttendanceDateAndCheckOutIsNullOrderByCheckInDesc(
                        studentId,
                        request.getAttendanceDate()
                )
                .or(() -> attendanceRepository.findFirstByStudent_StudentIdAndAttendanceDateOrderByCheckInDesc(
                        studentId,
                        request.getAttendanceDate()
                ))
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));
              attendance.processCheckOut(request.getCheckOut());

        attendanceCacheService.evictAttendanceCaches(
                attendance.getClassInfo().getAcademy().getId(),
                request.getAttendanceDate()
        );

        return AttendanceResponse.fromEntity(attendance);
    }


}
