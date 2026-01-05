package com.example.qoocca_be.student.service;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.repository.ClassInfoRepository;
import com.example.qoocca_be.student.model.*;
import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;
    private final ClassInfoRepository classInfoRepository;

    /* =========================
     * 학생 등록
     * ========================= */
    public StudentCreateResponse createStudent(
            Long classId,
            StudentCreateRequest request
    ) {
        ClassInfoEntity classInfo = classInfoRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 클래스"));

        StudentEntity student = StudentEntity.builder()
                .studentName(request.getStudentName())
                .studentStatus(
                        request.getStudentStatus() != null
                                ? request.getStudentStatus()
                                : StudentEntity.StudentStatus.ACTIVE
                )
                .classInfo(classInfo)
                .build();

        studentRepository.save(student);
        return StudentCreateResponse.from(student);
    }

    /* =========================
     * 수업별 학생 목록 조회
     * ========================= */
    @Transactional(readOnly = true)
    public List<StudentListResponse> getStudentsByClass(Long classId) {
        return studentRepository.findByClassInfo_ClassId(classId)
                .stream()
                .map(StudentListResponse::from)
                .toList();
    }

    /* =========================
     * 학생 단건 조회
     * ========================= */
    @Transactional(readOnly = true)
    public StudentDetailResponse getStudent(
            Long classId,
            Long studentId
    ) {
        StudentEntity student = studentRepository
                .findByStudentIdAndClassInfo_ClassId(studentId, classId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없음"));

        return StudentDetailResponse.from(student);
    }

    /* =========================
     * 학생 수정 (Dirty Checking)
     * ========================= */
    public StudentUpdateResponse updateStudent(
            Long classId,
            Long studentId,
            StudentUpdateRequest request
    ) {
        StudentEntity student = studentRepository
                .findByStudentIdAndClassInfo_ClassId(studentId, classId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없음"));

        // 🔥 Dirty Checking 핵심
        if (request.getStudentName() != null) {
            student.setStudentName(request.getStudentName());
        }

        if (request.getStudentStatus() != null) {
            student.setStudentStatus(request.getStudentStatus());
        }

        // save() 필요 없음
        return StudentUpdateResponse.from(student);
    }

    /* =========================
     * 학생 삭제 (Soft Delete + Dirty Checking)
     * ========================= */
    public StudentDeleteResponse deleteStudent(
            Long classId,
            Long studentId
    ) {
        StudentEntity student = studentRepository
                .findByStudentIdAndClassInfo_ClassId(studentId, classId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없음"));

        // 🔥 Soft Delete
        student.setStudentStatus(StudentEntity.StudentStatus.WITHDRAWN);

        // save() 필요 없음
        return StudentDeleteResponse.from(student);
    }
}
