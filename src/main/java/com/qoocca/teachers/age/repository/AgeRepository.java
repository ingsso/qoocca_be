package com.qoocca.teachers.age.repository;

import com.qoocca.teachers.age.entity.AgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgeRepository extends JpaRepository<AgeEntity, Long> {
}
