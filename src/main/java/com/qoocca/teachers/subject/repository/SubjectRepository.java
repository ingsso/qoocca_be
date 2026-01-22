package com.qoocca.teachers.subject.repository;

import com.qoocca.teachers.subject.entity.SubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubjectRepository extends JpaRepository<SubjectEntity, Long> {
    List<SubjectEntity> findByMainSubjectCode(String mainCode);
}
