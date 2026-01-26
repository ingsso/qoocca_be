package com.qoocca.teachers.db.student.repository;

import com.qoocca.teachers.db.student.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository
        extends JpaRepository<StudentEntity, Long> {

}
