package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.request.AcademyCreateRequest;
import com.qoocca.teachers.api.academy.model.request.AcademyRequest;
import com.qoocca.teachers.api.user.service.UserService;
import com.qoocca.teachers.api.global.config.CacheConfig;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
import com.qoocca.teachers.db.age.repository.AgeRepository;
import com.qoocca.teachers.db.subject.repository.SubjectRepository;
import com.qoocca.teachers.db.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AcademyRegistrationService {

    private final UserService userService;
    private final AcademyRepository academyRepository;
    private final AgeRepository ageRepository;
    private final SubjectRepository subjectRepository;

    @Value("${file.upload.path}")
    private String imageSavePath;

    @Value("${file.upload.base-url}")
    private String imageBaseUrl;

    @Value("${academy.registration.slow-log-threshold-ms:2000}")
    private long slowLogThresholdMs;

    @Value("${academy.registration.verbose-log:false}")
    private boolean verboseLogEnabled;

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.ME_ACADEMIES, key = "#userId")
    public Long registerAcademy(AcademyCreateRequest req, Long userId) {
        long startNanos = System.nanoTime();
        UserEntity user = userService.findById(userId);
        long afterUserLookupNanos = System.nanoTime();

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
        long afterEntitySaveNanos = System.nanoTime();

        if (req.getCertificateFile() == null || req.getCertificateFile().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        String academyFolderPath = imageSavePath + academy.getId() + "/";
        File folder = new File(academyFolderPath);
        long fileSaveStartNanos = System.nanoTime();
        if (!folder.exists()) folder.mkdirs();
        long afterFolderReadyNanos = System.nanoTime();

        String certFileName = "cert_" + UUID.randomUUID() + "_" + req.getCertificateFile().getOriginalFilename();
        try {
            req.getCertificateFile().transferTo(new File(academyFolderPath + certFileName));
            academy.setCertificate(imageBaseUrl + academy.getId() + "/" + certFileName);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.ACADEMY_CERTIFICATE_SAVE_FAILED);
        }
        long afterCertTransferNanos = System.nanoTime();

        if (req.getImageFiles() != null && !req.getImageFiles().isEmpty()) {
            log.warn(
                    "academy register ignored inline images academyId={}, userId={}, imageCount={}. " +
                            "Upload images via /api/academy/{id}/images",
                    academy.getId(),
                    userId,
                    req.getImageFiles().size()
            );
        }

        long afterFileSaveNanos = System.nanoTime();
        long totalMs = nanosToMs(afterFileSaveNanos - startNanos);
        long userLookupMs = nanosToMs(afterUserLookupNanos - startNanos);
        long dbWriteMs = nanosToMs(afterEntitySaveNanos - afterUserLookupNanos);
        long fileSaveMs = nanosToMs(afterFileSaveNanos - afterEntitySaveNanos);
        long folderReadyMs = nanosToMs(afterFolderReadyNanos - fileSaveStartNanos);
        long certTransferMs = nanosToMs(afterCertTransferNanos - afterFolderReadyNanos);
        long certSizeKb = req.getCertificateFile().getSize() / 1024;

        if (verboseLogEnabled || totalMs >= slowLogThresholdMs) {
            log.warn(
                    "academy register timing academyId={}, userId={}, totalMs={}, userLookupMs={}, dbWriteMs={}, fileSaveMs={}, folderReadyMs={}, certTransferMs={}, certSizeKb={}",
                    academy.getId(),
                    userId,
                    totalMs,
                    userLookupMs,
                    dbWriteMs,
                    fileSaveMs,
                    folderReadyMs,
                    certTransferMs,
                    certSizeKb
            );
        }

        return academy.getId();
    }

    private long nanosToMs(long nanos) {
        return nanos / 1_000_000;
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
