package com.qoocca.teachers.db.age.repository;

import com.qoocca.teachers.db.age.entity.AgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgeRepository extends JpaRepository<AgeEntity, Long> {
}
