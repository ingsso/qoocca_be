package com.example.qoocca_be.classInfo.repository;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.entity.ClassInfoStudentEntity;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassInfoStudentRepository extends JpaRepository<ClassInfoStudentEntity, Long> {

    List<ClassInfoStudentEntity> findByClassInfo_ClassId(Long classId);

    long countByClassInfo_ClassIdAndStatus(Long classId, StudentStatus status);

    boolean existsByClassInfo_ClassIdAndStudent_StudentId(Long classId, Long studentId);

    Optional<ClassInfoStudentEntity> findByClassInfo_ClassIdAndStudent_StudentId(Long classId, Long studentId);

    @Query("SELECT COUNT(DISTINCT c.student.studentId) FROM ClassInfoStudentEntity c " +
            "WHERE c.classInfo.academy.id = :academyId AND c.status = :status")
    long countUniqueStudentsByAcademy(@Param("academyId") Long academyId, @Param("status") StudentStatus status);

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
}
