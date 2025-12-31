package com.example.qoocca_be.age.repository;

import com.example.qoocca_be.age.entity.AgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgeRepository extends JpaRepository<AgeEntity, Long> {
}
