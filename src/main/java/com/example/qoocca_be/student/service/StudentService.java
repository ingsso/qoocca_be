package com.example.qoocca_be.student.service;

import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.model.StudentCreateRequest;
import com.example.qoocca_be.student.model.StudentResponse;
import com.example.qoocca_be.student.repository.StudentRepository;
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
                .studentStatus(
                        request.getStudentStatus() != null
                                ? request.getStudentStatus()
                                : StudentEntity.StudentStatus.ACTIVE
                )
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

        if (request.getStudentName() != null)
            student.setStudentName(request.getStudentName());

        if (request.getStudentStatus() != null)
            student.setStudentStatus(request.getStudentStatus());

        return StudentResponse.from(student);
    }

    public void delete(Long studentId) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 없음"));

        student.setStudentStatus(StudentEntity.StudentStatus.WITHDRAWN);
    }
}
