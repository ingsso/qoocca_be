package com.qoocca.be.subject.repository;

import com.qoocca.be.subject.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {
    List<SubjectEntity> findByMainSubjectCode(String mainCode);
}
