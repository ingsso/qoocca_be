package com.qoocca.teachers.db.classInfo.repository;

import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.model.ClassStatsProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClassInfoStatsRepository extends JpaRepository<ClassInfoEntity, Long> {

    @Query("""
        SELECT
            c.classId AS classId,
            c.className AS className,
            COUNT(cs.id) AS totalStudents,
            SUM(CASE WHEN cs.status = :status THEN 1 ELSE 0 END) AS withdrawnStudents
        FROM ClassInfoEntity c
        LEFT JOIN ClassInfoStudentEntity cs ON cs.classInfo = c
        WHERE c.academy.id = :academyId
        GROUP BY c.classId, c.className
    """)
    List<ClassStatsProjection> findClassStatsByAcademy(
            @Param("academyId") Long academyId,
            @Param("status") StudentStatus status
    );
}
