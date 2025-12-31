package com.example.qoocca_be.academy.repository;

import com.example.qoocca_be.academy.entity.AcademyEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademyRepository extends JpaRepository<AcademyEntity, Long> {
    List<AcademyEntity> findByUserId(Long userId);
    Optional<AcademyEntity> findByNameContaining(String name);
}
