package com.qoocca.teachers.academy.repository;

import com.qoocca.teachers.academy.entity.AcademyEntity;
import com.qoocca.teachers.academy.entity.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AcademyRepository extends JpaRepository<AcademyEntity, Long> {
    @Query("select a from AcademyEntity a " +
            "left join fetch a.academyImages " +
            "where a.id = :id")
    Optional<AcademyEntity> findDetailById(@Param("id") Long id);
    List<AcademyEntity> findAllByUserId(Long userId);

    Page<AcademyEntity> findAllByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);
}
