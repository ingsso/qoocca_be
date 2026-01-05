package com.example.qoocca_be.academy.service;

import com.example.qoocca_be.academy.dto.AcademyRequestDto;
import com.example.qoocca_be.academy.dto.AcademyResponseDto;
import com.example.qoocca_be.academy.dto.AcademySearchResponseDto;
import com.example.qoocca_be.academy.entity.AcademyAgeEntity;
import com.example.qoocca_be.academy.entity.AcademyEntity;
import com.example.qoocca_be.academy.entity.AcademySubjectEntity;
import com.example.qoocca_be.academy.entity.ApprovalStatus;
import com.example.qoocca_be.academy.repository.AcademyAgeRepository;
import com.example.qoocca_be.academy.repository.AcademyRepository;
import com.example.qoocca_be.academy.repository.AcademySubjectRepository;
import com.example.qoocca_be.age.model.AgeResponseDto;
import com.example.qoocca_be.age.repository.AgeRepository;
import com.example.qoocca_be.classInfo.repository.ClassInfoRepository;
import com.example.qoocca_be.global.common.PageResponseDto;
import com.example.qoocca_be.global.exception.CustomException;
import com.example.qoocca_be.global.exception.ErrorCode;
import com.example.qoocca_be.subject.model.SubjectResponseDto;
import com.example.qoocca_be.subject.repository.SubjectRepository;
import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademyService {

    private final UserService userService;
    private final ClassInfoRepository classInfoRepository;
    private final AcademyRepository academyRepository;
    private final AcademySubjectRepository academySubjectRepository;
    private final AgeRepository ageRepository;
    private final AcademyAgeRepository academyAgeRepository;
    private final SubjectRepository subjectRepository;

    @Transactional
    public Long registerAcademy(AcademyRequestDto req, Long userId) {
        UserEntity user = userService.findById(userId);

        AcademyEntity academy = AcademyEntity.builder()
                .name(req.getName())
                .baseAddress(req.getBaseAddress())
                .detailAddress(req.getDetailAddress())
                .briefInfo(req.getBriefInfo())
                .detailInfo(req.getDetailInfo())
                .phoneNumber(req.getPhoneNumber())
                .blogUrl(req.getBlogUrl())
                .websiteUrl(req.getWebsiteUrl())
                .instagramUrl(req.getInstagramUrl())
                .certificate(req.getCertificate())
                .user(user)
                .build();

        academy.updateAddress(req.getBaseAddress(), req.getDetailAddress());
        updateAcademyRelations(academy, req);

        return academyRepository.save(academy).getId();
    }

    @Transactional(readOnly = true)
    public List<SubjectResponseDto> getAcademySubjects(Long academyId) {
        List<AcademySubjectEntity> mappings = academySubjectRepository.findAllByAcademyId(academyId);

        return mappings.stream()
                .map(mapping -> mapping.getSubject().toResponseDto())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgeResponseDto> getAcademyAges(Long academyId) {
        List<AcademyAgeEntity> mappings = academyAgeRepository.findAllByAcademyId(academyId);

        return mappings.stream()
                .map(mapping -> mapping.getAge().toResponseDto())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademyResponseDto getAcademyDetail(Long id) {
        AcademyEntity academy = academyRepository.findDetailById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        return AcademyResponseDto.from(academy);
    }

//    @Transactional(readOnly = true)
//    public List<ClassInfoResponseDto> getClassesByAcademyId(Long academyId) {
//        // 1. 해당 학원이 존재하는지 먼저 검증 (선택 사항이지만 권장)
//        if (!academyRepository.existsById(academyId)) {
//            throw new CustomException(ErrorCode.ACADEMY_NOT_FOUND);
//        }
//
//        // 2. ClassInfoRepository에서 academyId로 데이터 조회
//        return classInfoRepository.findAllByAcademyId(academyId).stream()
//                .map(ClassInfoResponseDto::from) // Entity를 DTO로 변환
//                .collect(Collectors.toList());
//    }

    @Transactional
    public void updateAcademy(Long id, AcademyRequestDto req, Long userId) {
        AcademyEntity academy = academyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        academy.update(req);
        updateAcademyRelations(academy, req);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AcademySearchResponseDto> searchAcademiesByName(String name, Pageable pageable) {
        Page<AcademyEntity> academyPage = academyRepository.findByNameContaining(name, pageable);
        Page<AcademySearchResponseDto> dtoPage = academyPage.map(AcademySearchResponseDto::from);

        return new PageResponseDto<>(dtoPage);
    }

    private void updateAcademyRelations(AcademyEntity academy, AcademyRequestDto req) {
        if (req.getAgeIds() != null) {
            academy.updateAges(ageRepository.findAllById(req.getAgeIds()));
        }

        if (req.getSubjects() != null) {
            academy.updateSubjects(subjectRepository.findAllById(req.getSubjects()));
        }

        if (req.getImageUrls() != null) {
            academy.updateImages(req.getImageUrls());
        }
    }

    @Transactional
    public void approveAcademy(Long academyId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        academy.updateApprovalStatus(ApprovalStatus.APPROVED);
    }
}
