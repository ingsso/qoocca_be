package com.qoocca.teachers.api.attendance.service;

import com.qoocca.teachers.api.attendance.model.*;
import com.qoocca.teachers.api.classInfo.model.response.ClassSummaryResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.attendance.entity.AttendanceEntity;
import com.qoocca.teachers.db.attendance.repository.AttendanceRepository;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final StudentRepository studentRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;
    private final ClassInfoRepository classInfoRepository;

    /* =========================
     * 출결 등록
     * ========================= */
    public AttendanceResponse createAttendance(Long studentId, AttendanceCreateRequest request) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        ClassInfoEntity targetClass = findMatchingClass(studentId, request.getAttendanceDate(), request.getCheckIn());

        // 중복 등원 체크
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

    private ClassInfoEntity findMatchingClass(Long studentId, LocalDate attendanceDate, LocalTime checkIn) {
        List<ClassInfoEntity> classes = classInfoStudentRepository.findClassesByStudentId(studentId, StudentStatus.ENROLLED);

        String dayOfWeek = attendanceDate.getDayOfWeek().name().toLowerCase();

        return classes.stream()
                .filter(c -> isClassOnDay(c, dayOfWeek))
                .filter(c -> isTimeWithinRange(c, checkIn))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND_FOR_TIME));
    }

    @Transactional
    public AttendanceResponse updateCheckOut(Long studentId, LocalDate date) {
        AttendanceEntity attendance = attendanceRepository
                .findFirstByStudent_StudentIdAndAttendanceDateAndCheckOutIsNullOrderByCheckInDesc(studentId, date)
                .orElseGet(() -> attendanceRepository
                        .findByStudent_StudentIdAndAttendanceDate(studentId, date)
                        .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND)));

        attendance.processCheckOut(attendance.getClassInfo().getEndTime());

        return AttendanceResponse.fromEntity(attendance);
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
                .orElseThrow(() -> new CustomException(ErrorCode.ATTENDANCE_NOT_FOUND));

        return AttendanceResponse.fromEntity(attendance);
    }

    @Transactional(readOnly = true)
    public StudentCalendarResponse getStudentCalendarView(Long studentId, Long academyId, int year, int month) {
        // 1. 학생 정보 조회
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        // 2. 해당 학원에서 이 학생이 수강 중인 '재원' 상태의 클래스 목록 조회
        List<ClassInfoStudentEntity> enrollments = classInfoStudentRepository
                .findAllByAcademyAndStatus(academyId, StudentStatus.ENROLLED);

        List<String> enrolledClassNames = enrollments.stream()
                .filter(e -> e.getStudent().getStudentId().equals(studentId))
                .map(e -> e.getClassInfo().getClassName())
                .toList();

        // 3. 월간 출결 기록 조회 (작성하신 findByStudentAndAcademyAndDateBetween 활용)
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

    @Transactional(readOnly = true)
    public List<ClassAttendanceResponse> getTodayAttendanceByAcademy(Long academyId) {
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().name().toLowerCase();

        List<ClassInfoStudentEntity> todayEnrollments = classInfoStudentRepository
                .findAllByAcademyAndStatus(academyId, StudentStatus.ENROLLED).stream()
                .filter(enroll -> isClassOnDay(enroll.getClassInfo(), dayOfWeek))
                .toList();

        List<AttendanceEntity> todayAttendances = attendanceRepository.findByAttendanceDateWithDetails(today);

        return todayEnrollments.stream().map(enroll -> {
            StudentEntity student = enroll.getStudent();
            ClassInfoEntity classInfo = enroll.getClassInfo();

            Optional<AttendanceEntity> attendance = todayAttendances.stream()
                    .filter(a -> a.getStudent().getStudentId().equals(student.getStudentId())
                            && a.getClassInfo().getClassId().equals(classInfo.getClassId()))
                    .findFirst();

            return ClassAttendanceResponse.builder()
                    .studentId(student.getStudentId())
                    .studentName(student.getStudentName())
                    .className(classInfo.getClassName())
                    .checkIn(attendance.map(AttendanceEntity::getCheckIn).orElse(null))
                    .checkOut(attendance.map(AttendanceEntity::getCheckOut).orElse(null))
                    .status(attendance.map(a -> a.getStatus().name()).orElse("ABSENT"))
                    .statusLabel(formatStatusLabel(attendance))
                    .build();
        }).collect(Collectors.toList());
    }

    private String formatStatusLabel(Optional<AttendanceEntity> attendance) {
        // 1. 출결 기록 자체가 없는 경우 (미등원)
        if (attendance.isEmpty()) {
            return "미등원";
        }

        AttendanceEntity a = attendance.get();
        LocalTime checkIn = a.getCheckIn();
        LocalTime checkOut = a.getCheckOut();

        // 2. 등원 기록이 없는 경우 (기록은 생성되었으나 시간 데이터가 없는 예외 케이스 대비)
        if (checkIn == null) {
            return "미등원";
        }

        String checkInStr = checkIn.toString().substring(0, 5); // "13:00:00" -> "13:00" 포맷팅

        // 3. 하원 기록이 있는 경우
        if (checkOut != null) {
            return "등원 " + checkInStr + " ~ 하원 " + checkOut.toString().substring(0, 5);
        }

        // 4. 등원만 하고 하원 대기 중인 경우
        return "등원 " + checkInStr + " ~ 하원 대기";
    }

    @Transactional(readOnly = true)
    public List<ClassSummaryResponse> getClassSummariesByDate(Long academyId, LocalDate targetDate) {
        String dayOfWeek = targetDate.getDayOfWeek().name();

        List<ClassInfoEntity> classes = classInfoRepository.findAllByAcademyIdAndDayOfWeek(academyId, dayOfWeek);

        List<AttendanceEntity> allAttendances = attendanceRepository.findByAttendanceDateWithDetails(targetDate);

        return classes.stream().map(classInfo -> {
            List<ClassInfoStudentEntity> enrollments = classInfoStudentRepository.findAllByClassInfo_ClassIdAndStatus(
                    classInfo.getClassId(), StudentStatus.ENROLLED);

            List<AttendanceEntity> classAttendances = allAttendances.stream()
                    .filter(a -> a.getClassInfo().getClassId().equals(classInfo.getClassId()))
                    .toList();

            int totalStudents = enrollments.size();
            long presentCount = classAttendances.stream().filter(a -> "PRESENT".equals(a.getStatus().name())).count();
            long lateCount = classAttendances.stream().filter(a -> "LATE".equals(a.getStatus().name())).count();
            long absentCount = classAttendances.stream().filter(a -> "ABSENT".equals(a.getStatus().name())).count();

            int notPresentCount = totalStudents - classAttendances.size();

            return ClassSummaryResponse.builder()
                    .classId(classInfo.getClassId())
                    .className(classInfo.getClassName())
                    .classTime(classInfo.getStartTime() + "~" + classInfo.getEndTime())
                    .currentCount(totalStudents)
                    .presentCount(presentCount)
                    .lateCount(lateCount)
                    .absentCount(absentCount)
                    .notPresentCount(notPresentCount)
                    .build();
        }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentMonthlyStatResponse> getMonthlyStatsByClass(Long classId, int year, int month) {
        // 1. 해당 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 2. 해당 클래스의 모든 수강생 조회
        List<ClassInfoStudentEntity> enrollments = classInfoStudentRepository.findByClassInfo_ClassId(classId);

        // 3. 학생별로 기간 내 출결 기록을 가져와 통계 계산
        return enrollments.stream().map(enroll -> {
            StudentEntity student = enroll.getStudent();

            // 해당 기간 동안 이 학생의 모든 출결 기록 가져오기
            List<AttendanceEntity> records = attendanceRepository
                    .findByStudent_StudentIdAndClassInfo_ClassIdAndAttendanceDateBetween(student.getStudentId(), classId, startDate, endDate);

            return StudentMonthlyStatResponse.builder()
                    .studentId(student.getStudentId())
                    .studentName(student.getStudentName())
                    .presentCount(records.stream().filter(r -> r.getStatus() == AttendanceEntity.AttendanceStatus.PRESENT).count())
                    .lateCount(records.stream().filter(r -> r.getStatus() == AttendanceEntity.AttendanceStatus.LATE).count())
                    .absentCount(records.stream().filter(r -> r.getStatus() == AttendanceEntity.AttendanceStatus.ABSENT).count())
                    .build();
        }).collect(Collectors.toList());
    }
}
