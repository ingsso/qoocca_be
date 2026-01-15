package com.example.qoocca_be.student.repository;


import com.example.qoocca_be.student.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository
        extends JpaRepository<StudentEntity, Long> {

}
