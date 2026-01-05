package com.example.qoocca_be.student.service;

import com.example.qoocca_be.parent.entity.ParentEntity;
import com.example.qoocca_be.parent.model.ParentCreateRequest;
import com.example.qoocca_be.parent.model.ParentResponse;
import com.example.qoocca_be.parent.model.ParentUpdateRequest;
import com.example.qoocca_be.parent.repository.ParentRepository;
import com.example.qoocca_be.student.entity.StudentEntity;
import com.example.qoocca_be.student.entity.StudentParentEntity;
import com.example.qoocca_be.student.repository.StudentParentRepository;
import com.example.qoocca_be.student.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentParentService {

    private final StudentParentRepository studentParentRepository;
    private final ParentRepository parentRepository;
    private final StudentRepository studentRepository;

    /* =========================
     * GET: 학생의 부모 리스트 조회
     * ========================= */
    public List<ParentResponse> getParents(Long studentId) {
        List<StudentParentEntity> studentParents = studentParentRepository.findByStudent_StudentId(studentId);
        return studentParents.stream()
                .map(sp -> ParentResponse.from(sp.getParent()))
                .collect(Collectors.toList());
    }

    /* =========================
     * POST: 학생-부모 연결 생성
     * ========================= */
    public ParentResponse addParent(Long studentId, ParentCreateRequest request) {
        // 1. 학생 존재 확인
        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다. id=" + studentId));

        // 2. 부모 저장
        ParentEntity parent = ParentEntity.builder()
                .cardNum(request.getCardNum())
                .cardState(request.getCardState())
                .parentRelationship(request.getParentRelationship())
                .parentPhone(request.getParentPhone())
                .isPay(request.getIsPay())
                .alarm(request.getAlarm())
                .build();
        parent = parentRepository.save(parent);

        // 3. 학생-부모 연결
        StudentParentEntity studentParent = StudentParentEntity.builder()
                .student(student)
                .parent(parent)
                .build();
        studentParentRepository.save(studentParent);

        return ParentResponse.from(parent);
    }

    /* =========================
     * PUT: 부모 정보 수정
     * ========================= */
    public ParentResponse updateParent(Long studentId, Long parentId, ParentUpdateRequest request) {
        // 1. 학생-부모 관계 존재 확인
        StudentParentEntity sp = studentParentRepository.findByStudent_StudentIdAndParent_ParentId(studentId, parentId)
                .orElseThrow(() -> new IllegalArgumentException("학생-부모 관계를 찾을 수 없습니다."));

        ParentEntity parent = sp.getParent();

        // 2. 수정
        if (request.getCardNum() != null) parent.setCardNum(request.getCardNum());
        if (request.getCardState() != null) parent.setCardState(request.getCardState());
        if (request.getParentRelationship() != null) parent.setParentRelationship(request.getParentRelationship());
        if (request.getParentPhone() != null) parent.setParentPhone(request.getParentPhone());
        if (request.getIsPay() != null) parent.setIsPay(request.getIsPay());
        if (request.getAlarm() != null) parent.setAlarm(request.getAlarm());

        return ParentResponse.from(parent);
    }
}
