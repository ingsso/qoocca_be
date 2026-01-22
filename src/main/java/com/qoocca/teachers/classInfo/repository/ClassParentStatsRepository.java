package com.qoocca.teachers.classInfo.repository;

import com.qoocca.teachers.classInfo.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassParentStatsRepository extends JpaRepository<com.qoocca.teachers.classInfo.entity.ClassInfoEntity, Long> {

    @Query("""
        SELECT 
            c,
            s
        FROM ClassInfoEntity c
        JOIN ClassInfoStudentEntity cis ON cis.classInfo = c
        JOIN StudentEntity s ON cis.student = s
        WHERE c.academy.id = :academyId
          AND (:status IS NULL OR cis.status = :status)
    """)
    List<Object[]> findStudentsByAcademy(
            @Param("academyId") Long academyId,
            @Param("status") StudentStatus status
    );
}
