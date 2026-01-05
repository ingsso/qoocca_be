package com.example.qoocca_be.age.service;

import com.example.qoocca_be.age.entity.AgeEntity;
import com.example.qoocca_be.age.model.AgeResponseDto;
import com.example.qoocca_be.age.repository.AgeRepository;
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
    public List<AgeResponseDto> getAllAges() {
        return ageRepository.findAll().stream()
                .map(AgeEntity::toResponseDto)
                .collect(Collectors.toList());
    }
}
