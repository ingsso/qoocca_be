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

    /* =========================
     * 학생 기준
     * ========================= */

    // 학생 하루 출결
    Optional<AttendanceEntity> findByStudent_StudentIdAndAttendanceDate(
            Long studentId,
            LocalDate attendanceDate
    );

    // 학생 전체 출결
    List<AttendanceEntity> findByStudent_StudentId(Long studentId);

    // 학생 + 기간
    List<AttendanceEntity> findByStudent_StudentIdAndAttendanceDateBetween(
            Long studentId,
            LocalDate startDate,
            LocalDate endDate
    );

    /* =========================
     * 관리자 / 선생님 기준
     * ========================= */

    // 특정 날짜 전체 출결
    List<AttendanceEntity> findByAttendanceDate(LocalDate attendanceDate);

    // 상태별 전체 조회 (결석자, 지각자 등)
    List<AttendanceEntity> findByStatus(
            AttendanceEntity.AttendanceStatus status
    );

    // 날짜 + 상태 (오늘 결석자)
    List<AttendanceEntity> findByAttendanceDateAndStatus(
            LocalDate attendanceDate,
            AttendanceEntity.AttendanceStatus status
    );
}