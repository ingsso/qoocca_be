package com.qoocca.teachers.db.student.repository;

import com.qoocca.teachers.db.student.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository
        extends JpaRepository<StudentEntity, Long> {

    // 이름과 전화번호로 학생 조회
    Optional<StudentEntity> findByStudentNameAndStudentPhone(String studentName, String studentPhone);
}
