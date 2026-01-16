package com.example.qoocca_be.attendance.repository;

import com.example.qoocca_be.attendance.entity.AttendanceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
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

    // 날짜 + 상태
    List<AttendanceEntity> findByAttendanceDateAndStatus(
            LocalDate attendanceDate,
            AttendanceEntity.AttendanceStatus status
    );

    // 특정 클래스의 특정 날짜 출결 기록 리스트 조회
    List<AttendanceEntity> findByClassInfo_ClassIdAndAttendanceDate(Long classId, LocalDate attendanceDate);

    @Query("""
        SELECT COUNT(DISTINCT a.student.studentId)
        FROM AttendanceEntity a
        JOIN AcademyStudentEntity ast ON a.student.studentId = ast.student.studentId
        WHERE ast.academy.id = :academyId
          AND a.attendanceDate = :attendanceDate
          AND a.status IN :statuses
    """)
    Long countByAcademyAndDateAndStatusIn(
            @Param("academyId") Long academyId,
            @Param("attendanceDate") LocalDate attendanceDate,
            @Param("statuses") Collection<AttendanceEntity.AttendanceStatus> statuses
    );

    @Modifying
    @Transactional
    @Query("""
    INSERT INTO AttendanceEntity (student, classInfo, attendanceDate, status)
    SELECT cs.student, cs.classInfo, :today, 'ABSENT'
    FROM ClassInfoStudentEntity cs
    WHERE cs.status = 'ENROLLED'
      AND NOT EXISTS (
          SELECT 1 FROM AttendanceEntity a 
          WHERE a.student = cs.student 
            AND a.classInfo = cs.classInfo 
            AND a.attendanceDate = :today
      )
      AND cs.classInfo.endTime < :nowTime
""")
    int insertAbsenteesForFinishedClasses(@Param("today") LocalDate today,
                                          @Param("nowTime") LocalTime nowTime);
}