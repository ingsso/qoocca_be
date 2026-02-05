package com.qoocca.teachers.api.student.service;

import com.qoocca.teachers.db.parent.entity.ParentEntity;
import com.qoocca.teachers.api.parent.model.ParentCreateRequest;
import com.qoocca.teachers.api.parent.model.ParentResponse;
import com.qoocca.teachers.api.parent.model.ParentUpdateRequest;
import com.qoocca.teachers.db.parent.repository.ParentRepository;
import com.qoocca.teachers.db.student.entity.StudentEntity;
import com.qoocca.teachers.db.student.entity.StudentParentEntity;
import com.qoocca.teachers.db.student.repository.StudentParentRepository;
import com.qoocca.teachers.db.student.repository.StudentRepository;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;

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
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_NOT_FOUND));

        ParentEntity parent = parentRepository.findByParentPhone(request.getParentPhone())
                .orElseGet(() -> {
                    String cardNum = request.getCardNum();
                    boolean hasCard = (cardNum != null && !cardNum.isBlank());

                    ParentEntity newParent = ParentEntity.builder()
                            .parentName(request.getParentName())
                            .cardNum(hasCard ? cardNum : null)
                            .cardState(hasCard)
                            .parentRelationship(request.getParentRelationship())
                            .parentPhone(request.getParentPhone())
                            .isPay(request.getIsPay())
                            .alarm(request.getAlarm())
                            .build();
                    return parentRepository.save(newParent);
                });

        if (studentParentRepository
                .findByStudent_StudentIdAndParent_ParentId(studentId, parent.getParentId())
                .isPresent()) {
            return ParentResponse.from(parent);
        }

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
                .orElseThrow(() -> new CustomException(ErrorCode.STUDENT_PARENT_RELATION_NOT_FOUND));

        ParentEntity parent = sp.getParent();

        if (request.getParentName() != null) parent.setParentName(request.getParentName());
        boolean cardNumProvided = request.getCardNum() != null;
        if (cardNumProvided) {
            String cardNum = request.getCardNum();
            boolean hasCard = !cardNum.isBlank();

            parent.setCardNum(hasCard ? cardNum : null);
            parent.setCardState(hasCard);
        }

        if (request.getParentRelationship() != null) parent.setParentRelationship(request.getParentRelationship());
        if (request.getParentPhone() != null) parent.setParentPhone(request.getParentPhone());
        if (request.getIsPay() != null) parent.setIsPay(request.getIsPay());
        if (request.getAlarm() != null) parent.setAlarm(request.getAlarm());

        return ParentResponse.from(parent);
    }
}
