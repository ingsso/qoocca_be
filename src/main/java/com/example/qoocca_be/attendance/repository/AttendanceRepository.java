package com.example.qoocca_be.attendance.repository;

import com.example.qoocca_be.attendance.entity.AttendanceEntity;
import com.example.qoocca_be.classInfo.entity.ClassInfoStudentEntity;
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

    // 특정 학원에서 특정 학생의 일정 기간(예: 이번 달) 출결 데이터를 조회
    @Query("""
    SELECT a FROM AttendanceEntity a 
    WHERE a.student.studentId = :studentId 
      AND a.classInfo.academy.id = :academyId 
      AND a.attendanceDate BETWEEN :startDate AND :endDate
""")
    List<AttendanceEntity> findByStudentAndAcademyAndDateBetween(
            @Param("studentId") Long studentId,
            @Param("academyId") Long academyId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // 학생 ID + 클래스 ID + 기간으로 조회
    List<AttendanceEntity> findByStudent_StudentIdAndClassInfo_ClassIdAndAttendanceDateBetween(
            Long studentId,
            Long classId,
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

    // 학원의 특정 날짜에 특정 상태들(예: 출석+지각)에 해당하는 고유 학생 수를 카운트
    // 대시보드 통계용으로 사용
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

    // 자동 결석 처리 로직
    @Modifying
    @Transactional
    @Query("""
    INSERT INTO AttendanceEntity (student, classInfo, attendanceDate, status, createdAt, updatedAt)
    SELECT cs.student, cs.classInfo, :today, 'ABSENT', NOW(), NOW()
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

    // 출결 정보 조회 시 연관된 학생(student)과 클래스(classInfo) 정보를 한 번에 가져옴
    @Query("SELECT a FROM AttendanceEntity a " +
            "JOIN FETCH a.student " +
            "JOIN FETCH a.classInfo " +
            "WHERE a.attendanceDate = :attendanceDate")
    List<AttendanceEntity> findByAttendanceDateWithDetails(@Param("attendanceDate") LocalDate attendanceDate);


    // 특정 학생이 오늘 해당 수업에 대해 출결 처리 여부
    boolean existsByStudent_StudentIdAndClassInfo_ClassIdAndAttendanceDate(
            Long studentId, Long classId, LocalDate attendanceDate);
}