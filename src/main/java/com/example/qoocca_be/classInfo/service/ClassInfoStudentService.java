package com.example.qoocca_be.classInfo.service;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.entity.ClassInfoStudentEntity;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.classInfo.model.request.ClassStudentRequest;
import com.example.qoocca_be.classInfo.model.response.ClassStudentResponse;
import com.example.qoocca_be.classInfo.model.request.ClassStudentModifyRequest;

import com.example.qoocca_be.classInfo.repository.ClassInfoRepository;
import com.example.qoocca_be.classInfo.repository.ClassInfoStudentRepository;
import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassInfoStudentService {

    private final ClassInfoRepository classInfoRepository;
    private final StudentRepository studentRepository;
    private final ClassInfoStudentRepository repository;
    private final ClassInfoStudentRepository classInfoStudentRepository;
    /**
     * 기존 학생을 클래스에 배정
     */
    public void register(Long classId, ClassStudentRequest request) {

        // 이미 등록되어 있는지 체크
        if (repository.existsByClassInfo_ClassIdAndStudent_StudentId(classId, request.getStudentId())) {
            throw new IllegalStateException("이미 등록된 학생입니다.");
        }

        // 클래스 존재 확인
        ClassInfoEntity classInfo = classInfoRepository.findById(classId)
                .orElseThrow(() -> new IllegalArgumentException("클래스를 찾을 수 없습니다."));

        // 학생 존재 확인
        StudentEntity student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다."));

        // 기존 학생이므로 신규 생성 X, 바로 클래스-학생 매핑 테이블에 등록
        ClassInfoStudentEntity entity = ClassInfoStudentEntity.builder()
                .classInfo(classInfo)
                .student(student)
                .build();

        repository.save(entity);
    }

    public void modifyStatus(
            Long classId,
            Long studentId,
            ClassStudentModifyRequest request
    ) {
        ClassInfoStudentEntity entity = repository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("수강 정보가 존재하지 않습니다."));

        entity.setStatus(request.getStatus());
        // JPA Dirty Checking으로 자동 update
    }

    @Transactional
    public void moveStudent(Long classId, Long studentId, Long targetClassId) {

        // 기존 반 수강 정보 조회
        ClassInfoStudentEntity current = classInfoStudentRepository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("기존 반 수강 정보가 없습니다."));

        // 이동할 반 존재 확인
        ClassInfoEntity targetClass = classInfoRepository.findById(targetClassId)
                .orElseThrow(() -> new IllegalArgumentException("대상 반이 존재하지 않습니다."));

        // 중복 등록 방지
        if (classInfoStudentRepository.existsByClassInfo_ClassIdAndStudent_StudentId(targetClassId, studentId)) {
            throw new IllegalStateException("이미 대상 반에 등록된 학생입니다.");
        }

        StudentEntity student = current.getStudent();

        // 기존 관계 삭제
        classInfoStudentRepository.delete(current);

        // 새 관계 생성
        ClassInfoStudentEntity newEntity = ClassInfoStudentEntity.builder()
                .classInfo(targetClass)
                .student(student)
                .status(StudentStatus.ENROLLED)
                .build();

        classInfoStudentRepository.save(newEntity);
    }

    @Transactional(readOnly = true)
    public List<ClassStudentResponse> getStudents(Long classId) {
        return repository.findByClassInfo_ClassId(classId)
                .stream()
                .map(ClassStudentResponse::from)
                .toList();
    }

    public void remove(Long classId, Long studentId) {
        ClassInfoStudentEntity entity = repository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("수강 정보가 존재하지 않습니다."));

        repository.delete(entity);
    }
}
