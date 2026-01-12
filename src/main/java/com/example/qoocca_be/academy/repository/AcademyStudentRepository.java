package com.example.qoocca_be.academy.repository;

import com.example.qoocca_be.academy.entity.AcademyStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademyStudentRepository extends JpaRepository<AcademyStudentEntity, Long> {

    List<AcademyStudentEntity> findByAcademy_Id(Long academyId);

    boolean existsByAcademy_IdAndStudent_StudentId(Long academyId, Long studentId);

    Optional<AcademyStudentEntity> findByAcademy_IdAndStudent_StudentId(Long academyId, Long studentId);
}
