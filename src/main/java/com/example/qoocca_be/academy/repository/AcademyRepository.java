package com.example.qoocca_be.academy.repository;

import com.example.qoocca_be.academy.entity.AcademyEntity;
import com.example.qoocca_be.academy.entity.ApprovalStatus;
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
    // 유저가 가진 모든 학원 리스트 조회
    List<AcademyEntity> findAllByUserId(Long userId);

    Page<AcademyEntity> findAllByApprovalStatus(ApprovalStatus approvalStatus, Pageable pageable);
}
