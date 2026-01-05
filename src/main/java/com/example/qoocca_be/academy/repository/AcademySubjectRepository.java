package com.example.qoocca_be.academy.repository;

import com.example.qoocca_be.academy.entity.AcademySubjectEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AcademySubjectRepository extends JpaRepository<AcademySubjectEntity, Long> {
    @Query("select asub from AcademySubjectEntity asub " +
            "join fetch asub.subject " +
            "where asub.academy.id = :academyId")
    List<AcademySubjectEntity> findAllByAcademyId(@Param("academyId") Long academyId);
}
