package com.example.qoocca_be.attendance.model;

import com.example.qoocca_be.attendance.repository.AttendanceRepository;
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

    // 매시간 정각(0분 0초)에 실행
    @Scheduled(cron = "0 0 * * * *")
    public void autoAbsentProcess() {
        log.info("자동 결석 처리 스케줄러 시작");
        try {
            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();
            int count = attendanceRepository.insertAbsenteesForFinishedClasses(today, now);
            log.info("자동 결석 처리 완료: {}건의 결석 레코드가 생성되었습니다.", count);
        } catch (Exception e) {
            log.error("자동 결석 처리 중 에러 발생: ", e);
        }
    }
}