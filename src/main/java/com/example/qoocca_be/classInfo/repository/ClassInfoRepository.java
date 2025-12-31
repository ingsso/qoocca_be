package com.example.qoocca_be.classInfo.repository;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassInfoRepository
        extends JpaRepository<ClassInfoEntity, Long> {

}
