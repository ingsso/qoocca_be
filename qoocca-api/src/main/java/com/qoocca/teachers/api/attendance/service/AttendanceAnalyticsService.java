package com.qoocca.teachers.api.attendance.service;

import com.qoocca.teachers.api.attendance.model.ClassAttendanceResponse;
import com.qoocca.teachers.api.attendance.model.StudentMonthlyStatResponse;
import com.qoocca.teachers.api.classInfo.model.response.ClassSummaryResponse;
import com.qoocca.teachers.api.global.config.CacheConfig;
import com.qoocca.teachers.db.attendance.entity.AttendanceEntity;
import com.qoocca.teachers.db.attendance.repository.AttendanceRepository;
import com.qoocca.teachers.db.attendance.model.StudentMonthlyStatProjection;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
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
@Transactional(readOnly = true)
public class AttendanceAnalyticsService {

    private final AttendanceRepository attendanceRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;
    private final ClassInfoRepository classInfoRepository;

    @Cacheable(cacheNames = CacheConfig.ATTENDANCE_TODAY, key = "#academyId + ':' + T(java.time.LocalDate).now()")
    public List<ClassAttendanceResponse> getTodayAttendanceByAcademy(Long academyId) {
        LocalDate today = LocalDate.now();
        String dayOfWeek = today.getDayOfWeek().name().toLowerCase();

        List<ClassInfoStudentEntity> todayEnrollments = classInfoStudentRepository
                .findAllByAcademyAndStatus(academyId, StudentStatus.ENROLLED).stream()
                .filter(enroll -> AttendanceDayMatcher.isClassOnDay(enroll.getClassInfo(), dayOfWeek))
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

    @Cacheable(cacheNames = CacheConfig.ATTENDANCE_SUMMARY, key = "#academyId + ':' + #targetDate")
    public List<ClassSummaryResponse> getClassSummariesByDate(Long academyId, LocalDate targetDate) {
        String dayOfWeek = targetDate.getDayOfWeek().name().toLowerCase();

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

    public List<StudentMonthlyStatResponse> getMonthlyStatsByClass(Long classId, int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        List<StudentMonthlyStatProjection> stats = classInfoStudentRepository
                .findMonthlyStatsByClass(classId, StudentStatus.ENROLLED, startDate, endDate);

        return stats.stream()
                .map(s -> StudentMonthlyStatResponse.builder()
                        .studentId(s.getStudentId())
                        .studentName(s.getStudentName())
                        .presentCount(s.getPresentCount() == null ? 0 : s.getPresentCount())
                        .lateCount(s.getLateCount() == null ? 0 : s.getLateCount())
                        .absentCount(s.getAbsentCount() == null ? 0 : s.getAbsentCount())
                        .build())
                .collect(Collectors.toList());
    }

    private String formatStatusLabel(Optional<AttendanceEntity> attendance) {
        if (attendance.isEmpty()) {
            return "미등원";
        }

        AttendanceEntity a = attendance.get();
        LocalTime checkIn = a.getCheckIn();
        LocalTime checkOut = a.getCheckOut();

        if (checkIn == null) {
            return "미등원";
        }

        String checkInStr = checkIn.toString().substring(0, 5);

        if (checkOut != null) {
            return "등원 " + checkInStr + " ~ 하원 " + checkOut.toString().substring(0, 5);
        }

        return "등원 " + checkInStr + " ~ 하원 대기";
    }
}
