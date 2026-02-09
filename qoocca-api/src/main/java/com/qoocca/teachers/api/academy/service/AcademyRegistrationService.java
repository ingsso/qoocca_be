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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
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

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.ME_ACADEMIES, key = "#userId")
    public Long registerAcademy(AcademyCreateRequest req, Long userId) {
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

        String academyFolderPath = imageSavePath + academy.getId() + "/";
        File folder = new File(academyFolderPath);
        if (!folder.exists()) folder.mkdirs();

        if (req.getCertificateFile() != null && !req.getCertificateFile().isEmpty()) {
            String certFileName = "cert_" + UUID.randomUUID() + "_" + req.getCertificateFile().getOriginalFilename();
            try {
                req.getCertificateFile().transferTo(new File(academyFolderPath + certFileName));
                academy.setCertificate(imageBaseUrl + academy.getId() + "/" + certFileName);
            } catch (IOException e) {
                throw new CustomException(ErrorCode.ACADEMY_CERTIFICATE_SAVE_FAILED);
            }
        }

        List<String> finalImageUrls = new ArrayList<>();
        if (req.getImageFiles() != null && !req.getImageFiles().isEmpty()) {
            for (MultipartFile file : req.getImageFiles()) {
                if (file.isEmpty()) continue;

                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                try {
                    file.transferTo(new File(academyFolderPath + filename));
                    finalImageUrls.add(imageBaseUrl + academy.getId() + "/" + filename);
                } catch (IOException e) {
                    throw new CustomException(ErrorCode.ACADEMY_IMAGE_SAVE_FAILED);
                }
            }
        }

        if (!finalImageUrls.isEmpty()) {
            academy.updateImages(finalImageUrls);
        }

        return academy.getId();
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
