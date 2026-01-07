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
import io.jsonwebtoken.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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



    @Transactional
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
                .certificate(req.getCertificate())
                .user(user)
                .build();

        academy.updateAddress(req.getBaseAddress(), req.getDetailAddress());

        if (req.getAgeIds() != null) {
            academy.updateAges(ageRepository.findAllById(req.getAgeIds()));
        }

        if (req.getSubjects() != null) {
            academy.updateSubjects(subjectRepository.findAllById(req.getSubjects()));
        }

        if (req.getImageUrls() != null) {
            academy.updateImages(req.getImageUrls());
        }

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

    @Transactional
    public void updateAcademy(Long id, AcademyUpdateDto req, Long userId) {
        AcademyEntity academy = academyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        academy.update(req);

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

    // ---------------- 파일 업로드 지원 ----------------
    private final String IMAGE_BASE_URL = "http://localhost:8081/academy/";

    // nginx images 저장 경로 (호스트 기준 절대 경로, docker-compose volume과 매핑)
    private final String IMAGE_SAVE_PATH = "D:/fintech/login/qoocca_be/nginx/images/";

    @Transactional
    public Long registerAcademyWithFiles(AcademyCreateWithFilesRequest request, Long userId) throws IOException {
        UserEntity user = userService.findById(userId);

        AcademyEntity academy = AcademyEntity.builder()
                .name(request.getName())
                .baseAddress(request.getBaseAddress())
                .detailAddress(request.getDetailAddress())
                .briefInfo(request.getBriefInfo())
                .detailInfo(request.getDetailInfo())
                .phoneNumber(request.getPhoneNumber())
                .blogUrl(request.getBlogUrl())
                .websiteUrl(request.getWebsiteUrl())
                .instagramUrl(request.getInstagramUrl())
                .certificate(request.getCertificate())
                .user(user)
                .build();

        academy.updateAddress(request.getBaseAddress(), request.getDetailAddress());

        // DB 먼저 저장해서 ID 확보
        academyRepository.save(academy);

        // 이미지 처리
        List<MultipartFile> imageFiles = request.getImageFiles();
        if (imageFiles != null) {
            List<String> imageUrls = new ArrayList<>();

            // 학원별 폴더 경로
            String academyFolderPath = IMAGE_SAVE_PATH + academy.getId() + "/";
            File academyFolder = new File(academyFolderPath);
            if (!academyFolder.exists()) academyFolder.mkdirs();

            for (MultipartFile file : imageFiles) {
                if (file.isEmpty()) continue;

                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                File saveFile = new File(academyFolderPath + filename);
                try {
                    file.transferTo(saveFile);
                } catch (java.io.IOException e) {
                    throw new RuntimeException(e);
                }

                imageUrls.add(IMAGE_BASE_URL + academy.getId() + "/" + filename);
            }

            academy.updateImages(imageUrls);
        }

        return academy.getId();
    }







}

