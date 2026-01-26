package com.qoocca.teachers.db.classInfo.repository;

import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ClassParentStatsRepository extends JpaRepository<ClassInfoEntity, Long> {

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
