package com.example.qoocca_be.classInfo.repository;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClassInfoRepository extends JpaRepository<ClassInfoEntity, Long> {

    List<ClassInfoEntity> findByAcademy_Id(Long academyId);
}
