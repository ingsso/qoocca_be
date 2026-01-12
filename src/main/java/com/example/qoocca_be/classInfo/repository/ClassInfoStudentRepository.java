package com.example.qoocca_be.classInfo.repository;

import com.example.qoocca_be.classInfo.entity.ClassInfoStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassInfoStudentRepository extends JpaRepository<ClassInfoStudentEntity, Long> {

    List<ClassInfoStudentEntity> findByClassInfo_ClassId(Long classId);

    boolean existsByClassInfo_ClassIdAndStudent_StudentId(Long classId, Long studentId);

    Optional<ClassInfoStudentEntity> findByClassInfo_ClassIdAndStudent_StudentId(Long classId, Long studentId);
}
