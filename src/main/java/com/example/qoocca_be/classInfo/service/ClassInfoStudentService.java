package com.example.qoocca_be.classInfo.service;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import com.example.qoocca_be.classInfo.entity.ClassInfoStudentEntity;
import com.example.qoocca_be.classInfo.model.ClassInfoStudentRequestDTO;
import com.example.qoocca_be.classInfo.model.ClassInfoStudentResponseDTO;
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

    /**
     * 기존 학생을 클래스에 배정
     */
    public void register(Long classId, ClassInfoStudentRequestDTO request) {

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

    @Transactional(readOnly = true)
    public List<ClassInfoStudentResponseDTO> getStudents(Long classId) {
        return repository.findByClassInfo_ClassId(classId)
                .stream()
                .map(ClassInfoStudentResponseDTO::from)
                .toList();
    }

    public void remove(Long classId, Long studentId) {
        ClassInfoStudentEntity entity = repository
                .findByClassInfo_ClassIdAndStudent_StudentId(classId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("수강 정보가 존재하지 않습니다."));

        repository.delete(entity);
    }
}
