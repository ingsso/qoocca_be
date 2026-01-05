package com.example.qoocca_be.attendance.repository;

import com.example.qoocca_be.attendance.entity.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository
        extends JpaRepository<AttendanceEntity, Long> {

    // 학생 + 날짜로 출결 조회 (하루 1건)
    Optional<AttendanceEntity> findByStudent_StudentIdAndAttendanceDate(
            Long studentId,
            LocalDate attendanceDate
    );

    // 학생별 출결 전체 조회
    List<AttendanceEntity> findByStudent_StudentId(Long studentId);

    // 날짜별 출결 조회
    List<AttendanceEntity> findByAttendanceDate(LocalDate attendanceDate);

    // 상태별 출결 조회
    List<AttendanceEntity> findByStatus(AttendanceEntity.AttendanceStatus status);

    // 학생 + 기간 조회
    List<AttendanceEntity> findByStudent_StudentIdAndAttendanceDateBetween(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate
    );
}