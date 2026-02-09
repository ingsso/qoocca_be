package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.request.AcademyRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyResubmitRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyUpdateRequest;
import com.qoocca.teachers.api.academy.model.response.AcademyResponse;
import com.qoocca.teachers.api.age.model.AgeResponse;
import com.qoocca.teachers.api.subject.model.SubjectResponse;
import com.qoocca.teachers.api.global.config.CacheConfig;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.academy.entity.AcademyAgeEntity;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.AcademyImageEntity;
import com.qoocca.teachers.db.academy.entity.AcademySubjectEntity;
import com.qoocca.teachers.db.academy.entity.ApprovalStatus;
import com.qoocca.teachers.db.academy.repository.AcademyAgeRepository;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
import com.qoocca.teachers.db.academy.repository.AcademySubjectRepository;
import com.qoocca.teachers.db.age.repository.AgeRepository;
import com.qoocca.teachers.db.subject.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademyProfileService {

    private final AcademyRepository academyRepository;
    private final AcademySubjectRepository academySubjectRepository;
    private final AcademyAgeRepository academyAgeRepository;
    private final AgeRepository ageRepository;
    private final SubjectRepository subjectRepository;

    @Value("${file.upload.path}")
    private String imageSavePath;

    @Value("${file.upload.base-url}")
    private String imageBaseUrl;

    @Transactional(readOnly = true)
    public AcademyResponse getAcademyDetail(Long id) {
        AcademyEntity academy = academyRepository.findDetailById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));
        return AcademyResponse.from(academy);
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.ACADEMY_SUBJECTS, key = "#academyId")
    public List<SubjectResponse> getAcademySubjects(Long academyId) {
        List<AcademySubjectEntity> mappings = academySubjectRepository.findAllByAcademyId(academyId);
        return mappings.stream()
                .map(mapping -> SubjectResponse.from(mapping.getSubject()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.ACADEMY_AGES, key = "#academyId")
    public List<AgeResponse> getAcademyAges(Long academyId) {
        List<AcademyAgeEntity> mappings = academyAgeRepository.findAllByAcademyId(academyId);
        return mappings.stream()
                .map(mapping -> AgeResponse.from(mapping.getAge()))
                .collect(Collectors.toList());
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ME_ACADEMIES, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.ACADEMY_SUBJECTS, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.ACADEMY_AGES, key = "#id")
    })
    public void updateAcademy(Long id, AcademyUpdateRequest req, Long userId) {
        AcademyEntity academy = academyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        academy.updateInfo(
                req.getName(),
                req.getPhoneNumber(),
                req.getBriefInfo(),
                req.getDetailInfo()
        );

        academy.updateFullAddress(
                req.getBaseAddress(),
                req.getDetailAddress()
        );

        updateRelationalData(academy, req);

        // ✅ 인증서 처리
        if (req.getCertificateFile() != null && !req.getCertificateFile().isEmpty()) {
            updateCertificateFile(academy, req.getCertificateFile());
        }

    }

    @Transactional
    public void uploadAcademyImages(Long academyId, List<MultipartFile> images, Long userId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        String folderPath = imageSavePath + academy.getId() + "/";
        File folder = new File(folderPath);
        if (!folder.exists()) folder.mkdirs();

        for (MultipartFile file : images) {
            if (file.isEmpty()) continue;

            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            try {
                file.transferTo(new File(folderPath + filename));

                AcademyImageEntity image = AcademyImageEntity.builder()
                        .academy(academy)
                        .imageUrl(imageBaseUrl + academy.getId() + "/" + filename)
                        .build();

                // ✅ 기존 이미지 유지하면서 새 이미지만 추가
                academy.getAcademyImages().add(image);

            } catch (IOException e) {
                throw new CustomException(ErrorCode.ACADEMY_IMAGE_SAVE_FAILED);
            }
        }
    }


    /* ================= 이미지 삭제 ================= */
    @Transactional
    public void deleteAcademyImage(Long academyId, Long imageId, Long userId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        AcademyImageEntity image = academy.getAcademyImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_IMAGE_NOT_FOUND));

        // DB에서 먼저 제거
        academy.getAcademyImages().remove(image);

        // 실제 파일 삭제
        deletePhysicalFile(image.getImageUrl());
    }
    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ME_ACADEMIES, key = "#userId"),
            @CacheEvict(cacheNames = CacheConfig.ACADEMY_SUBJECTS, key = "#id"),
            @CacheEvict(cacheNames = CacheConfig.ACADEMY_AGES, key = "#id")
    })
    public void resubmitAcademy(Long id, AcademyResubmitRequest req, Long userId) {
        AcademyEntity academy = academyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        if (academy.getApprovalStatus() != ApprovalStatus.REJECTED) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        academy.updateInfo(req.getName(), null, null, null);
        academy.updateFullAddress(req.getBaseAddress(), req.getDetailAddress());

        if (req.getCertificateFile() != null && !req.getCertificateFile().isEmpty()) {
            updateCertificateFile(academy, req.getCertificateFile());
        }

        academy.updateApprovalStatus(ApprovalStatus.PENDING);
        academy.setRejectionReason(null);
    }

    private void deletePhysicalFile(String fileUrl) {
        try {
            String relativePath = fileUrl.replace(imageBaseUrl, "");
            File fileToDelete = new File(imageSavePath + relativePath);

            if (fileToDelete.exists()) {
                fileToDelete.delete();
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.ACADEMY_IMAGE_DELETE_FAILED);
        }
    }

    private void updateCertificateFile(AcademyEntity academy, MultipartFile certificateFile) {
        String academyFolderPath = imageSavePath + academy.getId() + "/";
        File folder = new File(academyFolderPath);
        if (!folder.exists()) folder.mkdirs();

        if (academy.getCertificate() != null) {
            deletePhysicalFile(academy.getCertificate());
        }

        String certFileName = "cert_" + UUID.randomUUID() + "_" + certificateFile.getOriginalFilename();
        try {
            certificateFile.transferTo(new File(academyFolderPath + certFileName));
            academy.setCertificate(imageBaseUrl + academy.getId() + "/" + certFileName);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.ACADEMY_CERTIFICATE_SAVE_FAILED);
        }
    }

    private void updateRelationalData(AcademyEntity academy, AcademyRequest req) {
        if (req.getAgeIds() != null) {
            academy.updateAges(ageRepository.findAllById(req.getAgeIds()));
        }

        if (req.getSubjects() != null) {
            academy.updateSubjects(subjectRepository.findAllById(req.getSubjects()));
        }
    }


}
