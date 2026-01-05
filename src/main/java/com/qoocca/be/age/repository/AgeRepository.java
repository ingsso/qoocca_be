package com.qoocca.be.age.repository;

import com.qoocca.be.age.entity.AgeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgeRepository extends JpaRepository<AgeEntity, Long> {
}
