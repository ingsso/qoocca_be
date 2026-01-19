package com.example.qoocca_be.academy.repository;

import com.example.qoocca_be.academy.entity.AcademyStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AcademyStudentRepository extends JpaRepository<AcademyStudentEntity, Long> {

    List<AcademyStudentEntity> findByAcademy_Id(Long academyId);

    Optional<AcademyStudentEntity> findByAcademy_IdAndStudent_StudentId(Long academyId, Long studentId);

    /**
     * 카드 미등록 학생 수 카운트
     * 1. 학원에 소속된 학생 중 (AcademyStudent)
     * 2. 부모 설정이 '수납 대상'이면서 (isPay = true)
     * 3. 결제 수단(카드) 상태가 유효하지 않은 (cardState = false) 경우 집계
     */
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
