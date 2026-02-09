package com.qoocca.teachers.db.classInfo.repository;

import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.attendance.model.StudentMonthlyStatProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClassInfoStudentRepository extends JpaRepository<ClassInfoStudentEntity, Long> {

    List<ClassInfoStudentEntity> findByClassInfo_ClassId(Long classId);

    boolean existsByClassInfo_ClassIdAndStudent_StudentId(Long classId, Long studentId);

    Optional<ClassInfoStudentEntity> findByClassInfo_ClassIdAndStudent_StudentId(Long classId, Long studentId);

    @Query("SELECT COUNT(DISTINCT c.student.studentId) FROM ClassInfoStudentEntity c " +
            "WHERE c.classInfo.academy.id = :academyId AND c.status = :status")
    long countUniqueStudentsByAcademy(@Param("academyId") Long academyId, @Param("status") StudentStatus status);

    @Query("SELECT cse FROM ClassInfoStudentEntity cse " +
            "JOIN FETCH cse.classInfo c " +
            "JOIN FETCH cse.student s " +
            "WHERE c.academy.id = :academyId AND cse.status = :status")
    List<ClassInfoStudentEntity> findAllByAcademyAndStatus(
            @Param("academyId") Long academyId,
            @Param("status") StudentStatus status
    );

    @Query("""
        SELECT cse.classInfo
        FROM ClassInfoStudentEntity cse
        WHERE cse.student.studentId = :studentId
          AND cse.status = :status
    """)
    List<ClassInfoEntity> findClassesByStudentId(
            @Param("studentId") Long studentId,
            @Param("status") StudentStatus status
    );

    @Query("""
    SELECT COUNT(DISTINCT cse.student.studentId)
    FROM ClassInfoStudentEntity cse
    JOIN cse.classInfo c
    WHERE c.academy.id = :academyId
      AND cse.status = :status
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
    long countExpectedStudentsToday(
            @Param("academyId") Long academyId,
            @Param("day") String day,
            @Param("status") StudentStatus status
    );

    List<ClassInfoStudentEntity> findAllByClassInfo_ClassIdAndStatus(Long classInfoClassId, StudentStatus status);

    @Query("""
        SELECT
            cse.student.studentId as studentId,
            cse.student.studentName as studentName,
            SUM(CASE WHEN a.status = 'PRESENT' THEN 1 ELSE 0 END) as presentCount,
            SUM(CASE WHEN a.status = 'LATE' THEN 1 ELSE 0 END) as lateCount,
            SUM(CASE WHEN a.status = 'ABSENT' THEN 1 ELSE 0 END) as absentCount
        FROM ClassInfoStudentEntity cse
        LEFT JOIN AttendanceEntity a
            ON a.student = cse.student
           AND a.classInfo = cse.classInfo
           AND a.attendanceDate BETWEEN :startDate AND :endDate
        WHERE cse.classInfo.classId = :classId
          AND cse.status = :status
        GROUP BY cse.student.studentId, cse.student.studentName
    """)
    List<StudentMonthlyStatProjection> findMonthlyStatsByClass(
            @Param("classId") Long classId,
            @Param("status") StudentStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
