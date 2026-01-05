package com.qoocca.be.academy.repository;

import com.qoocca.be.academy.entity.AcademyImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AcademyImageRepository extends JpaRepository<AcademyImageEntity, Long> {
    Optional<AcademyImageEntity> findByAcademyId(Long id);
}
