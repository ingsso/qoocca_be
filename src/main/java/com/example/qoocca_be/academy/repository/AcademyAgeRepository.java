package com.example.qoocca_be.academy.repository;

import com.example.qoocca_be.academy.entity.AcademyAgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademyAgeRepository extends JpaRepository<AcademyAgeEntity, Long> {
    List<AcademyAgeEntity> findAllByAcademyId(Long id);
}
