package com.example.qoocca_be.student.repository;


import com.example.qoocca_be.student.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository
        extends JpaRepository<StudentEntity, Long> {

    // 수업별 학생 목록 조회
    List<StudentEntity> findByClassInfo_ClassId(Long classId);

    // 수업 + 학생 단건 조회 (보안/정합성)
    Optional<StudentEntity> findByStudentIdAndClassInfo_ClassId(
            Long studentId,
            Long classId
    );
}
