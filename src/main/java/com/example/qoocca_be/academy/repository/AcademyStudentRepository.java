package com.example.qoocca_be.academy.repository;

import com.example.qoocca_be.academy.entity.AcademyStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AcademyStudentRepository extends JpaRepository<AcademyStudentEntity, Long> {

    List<AcademyStudentEntity> findByAcademy_Id(Long academyId);

    boolean existsByAcademy_IdAndStudent_StudentId(Long academyId, Long studentId);

    Optional<AcademyStudentEntity> findByAcademy_IdAndStudent_StudentId(Long academyId, Long studentId);

    @Query("""
    SELECT COUNT(DISTINCT ast.student.studentId)
    FROM AcademyStudentEntity ast
    JOIN StudentParentEntity sp ON ast.student.studentId = sp.student.studentId
    JOIN sp.parent p
    WHERE ast.academy.id = :academyId
      AND p.isPay = true
      AND p.cardState = false
""")
    long countStudentsWithoutCard(@Param("academyId") Long academyId);
}
