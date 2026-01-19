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
import jakarta.validation.Valid;

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

    public List<ParentResponse> getParents(Long studentId) {
        return studentParentRepository.findByStudent_StudentId(studentId)
                .stream()
                .map(sp -> ParentResponse.from(sp.getParent()))
                .collect(Collectors.toList());
    }

    public ParentResponse addParent(Long studentId, ParentCreateRequest request) {

        StudentEntity student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생을 찾을 수 없습니다. id=" + studentId));

        String cardNum = request.getCardNum();
        boolean hasCard = (cardNum != null && !cardNum.isBlank());

        ParentEntity parent = ParentEntity.builder()
                .parentName(request.getParentName())
                .cardNum(hasCard ? cardNum : null)
                .cardState(hasCard) // ✅ 프론트 값 무시하고 서버에서 결정
                .parentRelationship(request.getParentRelationship())
                .parentPhone(request.getParentPhone())
                .isPay(request.getIsPay())
                .alarm(request.getAlarm())
                .build();

        parentRepository.save(parent);

        StudentParentEntity studentParent = StudentParentEntity.builder()
                .student(student)
                .parent(parent)
                .build();

        studentParentRepository.save(studentParent);

        return ParentResponse.from(parent);
    }


    public ParentResponse updateParent(Long studentId, Long parentId, ParentUpdateRequest request) {

        StudentParentEntity sp = studentParentRepository
                .findByStudent_StudentIdAndParent_ParentId(studentId, parentId)
                .orElseThrow(() -> new IllegalArgumentException("학생-부모 관계를 찾을 수 없습니다."));

        ParentEntity parent = sp.getParent();

        if (request.getParentName() != null) parent.setParentName(request.getParentName());   // ✅ 추가
        if (request.getCardNum() != null) {
            String cardNum = request.getCardNum();
            boolean hasCard = !cardNum.isBlank();

            parent.setCardNum(hasCard ? cardNum : null);
            parent.setCardState(hasCard);
        }

        if (request.getCardState() != null) parent.setCardState(request.getCardState());
        if (request.getParentRelationship() != null) parent.setParentRelationship(request.getParentRelationship());
        if (request.getParentPhone() != null) parent.setParentPhone(request.getParentPhone());
        if (request.getIsPay() != null) parent.setIsPay(request.getIsPay());
        if (request.getAlarm() != null) parent.setAlarm(request.getAlarm());

        return ParentResponse.from(parent);
    }
}
