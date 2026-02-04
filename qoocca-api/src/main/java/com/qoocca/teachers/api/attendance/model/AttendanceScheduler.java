package com.qoocca.teachers.api.attendance.model;

import com.qoocca.teachers.db.attendance.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduler {

    private final AttendanceRepository attendanceRepository;

    // 기본 1시간 주기 실행 (설정값이 있으면 우선 적용)
    @Scheduled(cron = "${attendance.auto-absent.cron:0 0 * * * *}")
    public void autoAbsentProcess() {
        log.info("자동 결석 처리 스케줄러 시작");
        try {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            int count = attendanceRepository.insertAbsenteesForFinishedClasses(today, now);
            log.info("자동 결석 처리 완료: date={}, now={}, inserted={}", today, now, count);
        } catch (Exception e) {
            log.error("자동 결석 처리 중 오류", e);
        }
    }
}
