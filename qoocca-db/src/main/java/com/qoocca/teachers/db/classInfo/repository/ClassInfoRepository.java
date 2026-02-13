package com.qoocca.teachers.db.classInfo.repository;

import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.model.ClassSummaryDto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClassInfoRepository extends JpaRepository<ClassInfoEntity, Long> {

    List<ClassInfoEntity> findByAcademy_Id(Long academyId);

    Optional<ClassInfoEntity> findByAcademy_IdAndClassName(Long academyId, String className);

    // Age, Subject 정보를 한 번에 조회
    @Query("SELECT c FROM ClassInfoEntity c " +
            "JOIN FETCH c.age " +
            "JOIN FETCH c.subject " +
            "WHERE c.academy.id = :academyId")
    List<ClassInfoEntity> findByAcademy_IdWithDetails(@Param("academyId") Long academyId);

    /**
     * 특정 학원의 특정 요일에 수업이 있는 클래스 목록 조회
     */
    @Query("""
        SELECT c FROM ClassInfoEntity c
        WHERE c.academy.id = :academyId
          AND (
            (:day = 'monday' AND c.monday = true) OR
            (:day = 'tuesday' AND c.tuesday = true) OR
            (:day = 'wednesday' AND c.wednesday = true) OR
            (:day = 'thursday' AND c.thursday = true) OR
            (:day = 'friday' AND c.friday = true) OR
            (:day = 'saturday' AND c.saturday = true) OR
            (:day = 'sunday' AND c.sunday = true)
          )
    """)
    List<ClassInfoEntity> findAllByAcademyIdAndDayOfWeek(
            @Param("academyId") Long academyId,
            @Param("day") String day
    );

    @Query("""
    SELECT new com.qoocca.teachers.db.classInfo.model.ClassSummaryDto(
        c.classId,
        c.className,
        CAST(COUNT(DISTINCT cs.student.studentId) AS int),
        SUM(CASE WHEN a.status = 'PRESENT' THEN 1L ELSE 0L END),
        SUM(CASE WHEN a.status = 'LATE' THEN 1L ELSE 0L END),
        SUM(CASE WHEN a.status = 'ABSENT' THEN 1L ELSE 0L END)
    )
    FROM ClassInfoEntity c
    LEFT JOIN ClassInfoStudentEntity cs ON cs.classInfo = c AND cs.status = :status
    LEFT JOIN AttendanceEntity a ON a.student.studentId = cs.student.studentId
         AND a.classInfo.classId = c.classId
         AND a.attendanceDate = :today
    WHERE c.academy.id = :academyId
      AND (
          (:dayName = 'MONDAY' AND c.monday = true) OR
          (:dayName = 'TUESDAY' AND c.tuesday = true) OR
          (:dayName = 'WEDNESDAY' AND c.wednesday = true) OR
          (:dayName = 'THURSDAY' AND c.thursday = true) OR
          (:dayName = 'FRIDAY' AND c.friday = true) OR
          (:dayName = 'SATURDAY' AND c.saturday = true) OR
          (:dayName = 'SUNDAY' AND c.sunday = true)
      )
    GROUP BY c.classId, c.className
""")
    List<ClassSummaryDto> findDashboardSummaries(
            @Param("academyId") Long academyId,
            @Param("today") LocalDate today,
            @Param("dayName") String dayName,
            @Param("status") StudentStatus status
    );


}
