package com.example.qoocca_be.classInfo.repository;

import com.example.qoocca_be.classInfo.entity.ClassInfoStudentEntity;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClassInfoStudentRepository extends JpaRepository<ClassInfoStudentEntity, Long> {

    List<ClassInfoStudentEntity> findByClassInfo_ClassId(Long classId);

    boolean existsByClassInfo_ClassIdAndStudent_StudentId(Long classId, Long studentId);

    Optional<ClassInfoStudentEntity> findByClassInfo_ClassIdAndStudent_StudentId(Long classId, Long studentId);

    @Query("SELECT COUNT(DISTINCT c.student.studentId) " +
            "FROM ClassInfoStudentEntity c " +
            "WHERE c.classInfo.academy.id = :academyId " +
            "AND c.status = :status")
    long countUniqueStudentsByAcademyId(@Param("academyId") Long academyId, @Param("status") StudentStatus status);

    @Query("SELECT COUNT(c) FROM ClassInfoStudentEntity c WHERE c.classInfo.classId = :classId AND c.status = :status")
    long countByClassIdAndStatus(@Param("classId") Long classId, @Param("status") StudentStatus status);
}
