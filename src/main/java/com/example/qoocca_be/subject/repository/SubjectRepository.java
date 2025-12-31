package com.example.qoocca_be.subject.repository;

import com.example.qoocca_be.subject.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {
}
