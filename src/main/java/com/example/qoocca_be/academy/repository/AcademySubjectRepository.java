package com.example.qoocca_be.academy.repository;

import com.example.qoocca_be.academy.entity.AcademySubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademySubjectRepository extends JpaRepository<AcademySubjectEntity, Long> {
    Optional<AcademySubjectEntity> findByAcademyId(Long id);
}
