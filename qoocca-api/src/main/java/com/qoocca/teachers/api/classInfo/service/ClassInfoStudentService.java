package com.qoocca.teachers.api.classInfo.service;

import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentRequest;
import com.qoocca.teachers.api.classInfo.model.response.ClassStudentResponse;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentModifyRequest;

import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStudentRepository;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.repository.StudentRepository;
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
            throw new CustomException(ErrorCode.STUDENT_ALREADY_ENROLLED);
        }

        // 클래스 존재 확인
        ClassInfoEntity classInfo = classInfoRepository.findById(classId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        // 학생 존재 확인
        StudentEntity student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

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
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        entity.setStatus(request.getStatus());
        // JPA Dirty Checking으로 자동 update
    }

    @Transactional
    public void moveStudent(Long classId, Long studentId, Long targetClassId) {

        // 기존 반 수강 정보 조회
        ClassInfoStudentEntity current = classInfoStudentRepository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        // 이동할 반 존재 확인
        ClassInfoEntity targetClass = classInfoRepository.findById(targetClassId)
                .orElseThrow(() -> new CustomException(ErrorCode.CLASS_NOT_FOUND));

        // 중복 등록 방지
        if (classInfoStudentRepository.existsByClassInfo_ClassIdAndStudent_StudentId(targetClassId, studentId)) {
            throw new CustomException(ErrorCode.STUDENT_ALREADY_ENROLLED);
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
                .orElseThrow(() -> new CustomException(ErrorCode.ENROLLMENT_NOT_FOUND));

        repository.delete(entity);
    }
}
