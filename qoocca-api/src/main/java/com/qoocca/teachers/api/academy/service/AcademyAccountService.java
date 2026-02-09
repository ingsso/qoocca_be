package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.response.AcademyCheckResponse;
import com.qoocca.teachers.api.academy.model.response.AcademyListResponse;
import com.qoocca.teachers.api.global.config.CacheConfig;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.ApprovalStatus;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademyAccountService {

    private final AcademyRepository academyRepository;

    @Transactional(readOnly = true)
    public AcademyCheckResponse checkRegistrationStatus(Long userId) {
        List<AcademyEntity> academies = academyRepository.findAllByUserId(userId);

        if (academies.isEmpty()) {
            return new AcademyCheckResponse(false, null);
        }

        AcademyEntity approvedAcademy = academies.stream()
                .filter(a -> a.getApprovalStatus() == ApprovalStatus.APPROVED)
                .findFirst()
                .orElse(academies.get(0));

        return new AcademyCheckResponse(
                approvedAcademy.getApprovalStatus() == ApprovalStatus.APPROVED,
                approvedAcademy.getId()
        );
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.ME_ACADEMIES, key = "#userId")
    public List<AcademyListResponse> getMyAcademies(Long userId) {
        return academyRepository.findAllByUserId(userId).stream()
                .map(academy -> AcademyListResponse.builder()
                        .academyId(academy.getId())
                        .name(academy.getName())
                        .approvalStatus(academy.getApprovalStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
