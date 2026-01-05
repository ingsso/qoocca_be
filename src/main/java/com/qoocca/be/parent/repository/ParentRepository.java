package com.qoocca.be.parent.repository;


import com.qoocca.be.parent.entity.ParentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParentRepository
        extends JpaRepository<ParentEntity, Long> {

    Optional<ParentEntity> findByParentPhone(String parentPhone);
}
