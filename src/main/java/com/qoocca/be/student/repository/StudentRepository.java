package com.qoocca.be.student.repository;


import com.qoocca.be.student.entity.StudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository
        extends JpaRepository<StudentEntity, Long> {

   /* // 수업별 학생 조회
    List<StudentEntity> findByClassInfo_ClassId(Long classId);

    // 상태별 학생 조회
    List<StudentEntity> findByStudentStatus(StudentEntity.StudentStatus status);

    // 수업 + 상태별 조회
    List<StudentEntity> findByClassInfo_ClassIdAndStudentStatus(
            Long classId,
            StudentEntity.StudentStatus status
    );*/
}
