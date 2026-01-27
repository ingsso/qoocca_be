package com.qoocca.teachers.api.student.service;

import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.api.student.model.StudentCreateRequest;
import com.qoocca.teachers.api.student.model.StudentResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.student.repository.StudentRepository;
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
                .studentPhone(request.getStudentPhone())
                .build();

        studentRepository.save(student);
        return StudentResponse.from(student);
    }


    @Transactional(readOnly = true)
    public StudentResponse get(Long studentId) {
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        return StudentResponse.from(student);
    }

    public StudentResponse update(Long studentId, StudentCreateRequest request) {

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

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
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        studentRepository.delete(student); // 또는 soft delete 컬럼 추가 가능
    }
}
