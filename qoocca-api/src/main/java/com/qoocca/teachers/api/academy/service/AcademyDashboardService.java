package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.response.DashboardStatsResponse;
import com.qoocca.teachers.api.global.config.CacheConfig;
import com.qoocca.teachers.db.academy.repository.AcademyStudentRepository;
import com.qoocca.teachers.db.attendance.entity.AttendanceEntity;
import com.qoocca.teachers.db.attendance.repository.AttendanceRepository;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.db.receipt.repository.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AcademyDashboardService {

    private final AcademyStudentRepository academyStudentRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;
    private final ReceiptRepository receiptRepository;

    @Cacheable(cacheNames = CacheConfig.DASHBOARD_STATS, key = "#academyId")
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(Long academyId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59);
        String dayOfWeek = today.getDayOfWeek().name().toLowerCase();

        List<AttendanceEntity.AttendanceStatus> activeStatuses = List.of(
                AttendanceEntity.AttendanceStatus.PRESENT,
                AttendanceEntity.AttendanceStatus.LATE,
                AttendanceEntity.AttendanceStatus.EARLY_LEAVE
        );

        Long studentCount = academyStudentRepository.countStudentsByAcademy(academyId);
        Long totalTodayCount = classInfoStudentRepository.countExpectedStudentsToday(
                academyId,
                dayOfWeek,
                StudentStatus.ENROLLED
        );
        Long presentCount = attendanceRepository.countByAcademyAndDateAndStatusIn(
                academyId,
                today,
                activeStatuses
        );
        Long noCardCount = academyStudentRepository.countStudentsWithoutCard(academyId);
        Long totalMonthlyFee = receiptRepository.sumAmountByAcademyAndPeriod(academyId, startOfMonth, endOfMonth);

        return DashboardStatsResponse.builder()
                .studentCount(studentCount)
                .presentCount(presentCount)
                .totalTodayCount(totalTodayCount)
                .noCardCount(noCardCount)
                .totalMonthlyFee(totalMonthlyFee != null ? totalMonthlyFee : 0L)
                .build();
    }
}
