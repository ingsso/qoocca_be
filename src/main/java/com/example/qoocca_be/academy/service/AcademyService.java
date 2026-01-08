package com.example.qoocca_be.academy.service;

import com.example.qoocca_be.academy.dto.*;
import com.example.qoocca_be.academy.entity.AcademyAgeEntity;
import com.example.qoocca_be.academy.entity.AcademyEntity;
import com.example.qoocca_be.academy.entity.AcademySubjectEntity;
import com.example.qoocca_be.academy.entity.ApprovalStatus;
import com.example.qoocca_be.academy.repository.AcademyAgeRepository;
import com.example.qoocca_be.academy.repository.AcademyRepository;
import com.example.qoocca_be.academy.repository.AcademySubjectRepository;
import com.example.qoocca_be.age.model.AgeResponseDto;
import com.example.qoocca_be.age.repository.AgeRepository;
import com.example.qoocca_be.global.common.PageResponseDto;
import com.example.qoocca_be.global.exception.CustomException;
import com.example.qoocca_be.global.exception.ErrorCode;
import com.example.qoocca_be.subject.model.SubjectResponseDto;
import com.example.qoocca_be.subject.repository.SubjectRepository;
import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademyService {

    private final UserService userService;
    private final AcademyRepository academyRepository;
    private final AcademySubjectRepository academySubjectRepository;
    private final AgeRepository ageRepository;
    private final AcademyAgeRepository academyAgeRepository;
    private final SubjectRepository subjectRepository;

    @Value("${file.upload.path}")
    private String IMAGE_SAVE_PATH;

    @Value("${file.upload.base-url}")
    private String IMAGE_BASE_URL;

    @Transactional
    public Long registerAcademy(AcademyCreateRequestDto req, Long userId) {
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
                .user(user)
                .build();

        academy.updateAddress(req.getBaseAddress(), req.getDetailAddress());
        updateRelationalData(academy, req);

        academyRepository.save(academy);

        String academyFolderPath = IMAGE_SAVE_PATH + academy.getId() + "/";
        File folder = new File(academyFolderPath);
        if (!folder.exists()) folder.mkdirs();

        if (req.getCertificateFile() != null && !req.getCertificateFile().isEmpty()) {
            String certFileName = "cert_" + UUID.randomUUID() + "_" + req.getCertificateFile().getOriginalFilename();
            try {
                req.getCertificateFile().transferTo(new File(academyFolderPath + certFileName));
                academy.setCertificate(IMAGE_BASE_URL + academy.getId() + "/" + certFileName);
            } catch (IOException e) {
                throw new RuntimeException("사업자 등록증 저장 실패", e);
            }
        }

        List<String> finalImageUrls = new ArrayList<>();
        if (req.getImageFiles() != null && !req.getImageFiles().isEmpty()) {
            for (MultipartFile file : req.getImageFiles()) {
                if (file.isEmpty()) continue;

                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                try {
                    file.transferTo(new File(academyFolderPath + filename));
                    finalImageUrls.add(IMAGE_BASE_URL + academy.getId() + "/" + filename);
                } catch (IOException e) {
                    throw new RuntimeException("이미지 파일 저장 실패", e);
                }
            }
        }

        if (!finalImageUrls.isEmpty()) {
            academy.updateImages(finalImageUrls);
        }

        return academy.getId();
    }

    @Transactional(readOnly = true)
    public AcademyResponseDto getAcademyDetail(Long id) {
        AcademyEntity academy = academyRepository.findDetailById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        return AcademyResponseDto.from(academy);
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

    @Transactional
    public void updateAcademy(Long id, AcademyUpdateDto req, Long userId) {
        AcademyEntity academy = academyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        academy.update(req);
        updateRelationalData(academy, req);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AcademySearchResponseDto> searchAcademiesByName(String name, Pageable pageable) {
        Page<AcademyEntity> academyPage = academyRepository.findByNameContaining(name, pageable);
        Page<AcademySearchResponseDto> dtoPage = academyPage.map(AcademySearchResponseDto::from);

        return new PageResponseDto<>(dtoPage);
    }

    @Transactional
    public void approveAcademy(Long academyId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        academy.updateApprovalStatus(ApprovalStatus.APPROVED);
    }

    @Transactional
    public void rejectAcademy(Long academyId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        academy.updateApprovalStatus(ApprovalStatus.REJECTED);
    }

    @Transactional(readOnly = true)
    public PageResponseDto<AcademySearchResponseDto> getPendingAcademies(Pageable pageable) {
        Page<AcademyEntity> pendingPage = academyRepository.findAllByApprovalStatus(ApprovalStatus.PENDING, pageable);

        Page<AcademySearchResponseDto> dtoPage = pendingPage.map(AcademySearchResponseDto::from);

        return new PageResponseDto<>(dtoPage);
    }

    private void updateRelationalData(AcademyEntity academy, AcademyRequest req) {
        if (req.getAgeIds() != null) {
            academy.updateAges(ageRepository.findAllById(req.getAgeIds()));
        }

        if (req.getSubjects() != null) {
            academy.updateSubjects(subjectRepository.findAllById(req.getSubjects()));
        }

        if (req instanceof AcademyUpdateDto updateDto) {
            if (updateDto.getImageUrls() != null) {
                academy.updateImages(updateDto.getImageUrls());
            }
        }
    }

}

