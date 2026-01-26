package com.qoocca.teachers.api.subject.service;

import com.qoocca.teachers.api.subject.model.SubjectResponse;
import com.qoocca.teachers.db.subject.repository.SubjectRepository;
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
                .map(SubjectResponse::from)
                .collect(Collectors.toList());
    }
}
