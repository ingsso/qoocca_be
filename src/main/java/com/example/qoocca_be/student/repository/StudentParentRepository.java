package com.example.qoocca_be.student.repository;

import com.example.qoocca_be.student.entity.StudentParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentParentRepository
        extends JpaRepository<StudentParentEntity, Long> {

    /* =========================
     * 기본 조회
     * ========================= */

    // 학생 기준 → 연결된 부모 목록
    List<StudentParentEntity> findByStudent_StudentId(Long studentId);

    // 부모 기준 → 연결된 학생 목록
    List<StudentParentEntity> findByParent_ParentId(Long parentId);

    /* =========================
     * 중복 방지 / 단건 조회
     * ========================= */

    // 특정 학생-부모 관계 존재 여부 확인
    boolean existsByStudent_StudentIdAndParent_ParentId(
            Long studentId,
            Long parentId
    );

    // 특정 학생-부모 관계 단건 조회
    Optional<StudentParentEntity> findByStudent_StudentIdAndParent_ParentId(
            Long studentId,
            Long parentId
    );

    /* =========================
     * 삭제
     * ========================= */

    // 학생 기준 전체 부모 연결 삭제 (학생 삭제 시)
    void deleteByStudent_StudentId(Long studentId);

    // 부모 기준 전체 학생 연결 삭제 (부모 삭제 시)
    void deleteByParent_ParentId(Long parentId);
}
