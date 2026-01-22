package com.qoocca.teachers.age.service;

import com.qoocca.teachers.age.entity.AgeEntity;
import com.qoocca.teachers.age.model.AgeResponse;
import com.qoocca.teachers.age.repository.AgeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgeService {

    private final AgeRepository ageRepository;

    @Transactional(readOnly = true)
    public List<AgeResponse> getAllAges() {
        return ageRepository.findAll().stream()
                .map(AgeEntity::toResponseDto)
                .collect(Collectors.toList());
    }
}
