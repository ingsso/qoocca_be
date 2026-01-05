package com.qoocca.be.academy.repository;

import com.qoocca.be.academy.entity.AcademyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AcademyRepository extends JpaRepository<AcademyEntity, Long> {
    @Query("select a from AcademyEntity a " +
            "left join fetch a.images " +
            "where a.id = :id")
    Optional<AcademyEntity> findDetailById(@Param("id") Long id);
    Optional<AcademyEntity> findByUserId(Long userId);
    Page<AcademyEntity> findByNameContaining(String name, Pageable pageable);
}
