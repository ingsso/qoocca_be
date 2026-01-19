package com.example.qoocca_be.subject.service;

import com.example.qoocca_be.subject.entity.SubjectEntity;
import com.example.qoocca_be.subject.model.SubjectResponse;
import com.example.qoocca_be.subject.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubjectService {

    private final SubjectRepository subjectRepository;

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(SubjectEntity::toResponseDto)
                .collect(Collectors.toList());
    }
}
