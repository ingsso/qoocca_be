package com.qoocca.teachers.db.academy.repository;

import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.ApprovalStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @EntityGraph(attributePaths = {"user", "academyImages", "academyAges", "academySubjects"})
    Optional<AcademyEntity> findAdminDetailById(Long id);
    List<AcademyEntity> findAllByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    Page<AcademyEntity> findAllByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = "user")
    Page<AcademyEntity> findAll(Pageable pageable);
}
