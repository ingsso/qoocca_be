package com.qoocca.teachers.academy.repository;

import com.qoocca.teachers.academy.entity.AcademyAgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AcademyAgeRepository extends JpaRepository<AcademyAgeEntity, Long> {
    List<AcademyAgeEntity> findAllByAcademyId(Long id);
}
