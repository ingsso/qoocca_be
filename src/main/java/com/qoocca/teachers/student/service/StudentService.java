package com.qoocca.teachers.student.service;

import com.qoocca.teachers.student.entity.StudentEntity;
import com.qoocca.teachers.student.model.StudentCreateRequest;
import com.qoocca.teachers.student.model.StudentResponse;
import com.qoocca.teachers.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentResponse create(StudentCreateRequest request) {

        StudentEntity student = StudentEntity.builder()
                .studentName(request.getStudentName())
                .studentPhone(request.getStudentPhone()) // ✅ 추가
                .build();

        studentRepository.save(student);
        return StudentResponse.from(student);
    }


    @Transactional(readOnly = true)
    public StudentResponse get(Long studentId) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 없음"));

        return StudentResponse.from(student);
    }

    public StudentResponse update(Long studentId, StudentCreateRequest request) {

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 없음"));

        if (request.getStudentName() != null) {
            student.setStudentName(request.getStudentName());
        }

        if (request.getStudentPhone() != null) {          // ✅ 추가
            student.setStudentPhone(request.getStudentPhone());
        }

        return StudentResponse.from(student);
    }


    public void delete(Long studentId) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 없음"));

        studentRepository.delete(student); // 또는 soft delete 컬럼 추가 가능
    }
}
