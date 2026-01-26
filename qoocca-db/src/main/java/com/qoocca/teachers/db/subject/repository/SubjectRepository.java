package com.qoocca.teachers.db.subject.repository;

import com.qoocca.teachers.db.subject.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {
}
