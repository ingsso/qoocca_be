package com.example.qoocca_be.classInfo.repository;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.classInfo.model.ClassSummaryResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ClassInfoRepository extends JpaRepository<ClassInfoEntity, Long> {

    List<ClassInfoEntity> findByAcademy_Id(Long academyId);

    @Query("""
    SELECT new com.example.qoocca_be.classInfo.model.ClassSummaryResponse(
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
    GROUP BY c.classId, c.className
""")
    List<ClassSummaryResponse> findDashboardSummaries(
            @Param("academyId") Long academyId,
            @Param("today") LocalDate today,
            @Param("status") StudentStatus status
    );
}
