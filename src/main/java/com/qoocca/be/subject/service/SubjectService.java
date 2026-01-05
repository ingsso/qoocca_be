package com.qoocca.be.subject.service;

import com.qoocca.be.subject.entity.SubjectEntity;
import com.qoocca.be.subject.model.SubjectResponseDto;
import com.qoocca.be.subject.repository.SubjectRepository;
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
    public List<SubjectResponseDto> getAllSubjects() {
        return subjectRepository.findAll().stream()
                .map(SubjectEntity::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SubjectResponseDto> getSubjectByMainCode(String mainCode) {
        return subjectRepository.findByMainSubjectCode(mainCode).stream()
                .map(SubjectEntity::toResponseDto)
                .collect(Collectors.toList());
    }
}
