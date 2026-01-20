package com.example.qoocca_be.academy.service;

import com.example.qoocca_be.academy.entity.*;
import com.example.qoocca_be.academy.model.request.AcademyCreateRequest;
import com.example.qoocca_be.academy.model.request.AcademyRequest;
import com.example.qoocca_be.academy.model.request.AcademyUpdateRequest;
import com.example.qoocca_be.academy.model.response.*;
import com.example.qoocca_be.academy.repository.AcademyAgeRepository;
import com.example.qoocca_be.academy.repository.AcademyRepository;
import com.example.qoocca_be.academy.repository.AcademyStudentRepository;
import com.example.qoocca_be.academy.repository.AcademySubjectRepository;
import com.example.qoocca_be.age.model.AgeResponse;
import com.example.qoocca_be.age.repository.AgeRepository;
import com.example.qoocca_be.attendance.entity.AttendanceEntity;
import com.example.qoocca_be.attendance.repository.AttendanceRepository;
import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.classInfo.repository.ClassInfoStudentRepository;
import com.example.qoocca_be.global.common.PageResponseDto;
import com.example.qoocca_be.global.exception.CustomException;
import com.example.qoocca_be.global.exception.ErrorCode;
import com.example.qoocca_be.receipt.repository.ReceiptRepository;
import com.example.qoocca_be.subject.model.SubjectResponse;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AcademyService {

    private final UserService userService;
    private final AcademyRepository academyRepository;
    private final AcademyStudentRepository academyStudentRepository;
    private final AcademySubjectRepository academySubjectRepository;
    private final AgeRepository ageRepository;
    private final AcademyAgeRepository academyAgeRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceRepository attendanceRepository;
    private final ClassInfoStudentRepository classInfoStudentRepository;
    private final ReceiptRepository receiptRepository;

    @Value("${file.upload.path}")
    private String IMAGE_SAVE_PATH;

    @Value("${file.upload.base-url}")
    private String IMAGE_BASE_URL;

    @Transactional(readOnly = true)
    public AcademyCheckResponse checkRegistrationStatus(Long userId) {
        List<AcademyEntity> academies = academyRepository.findAllByUserId(userId);

        if (academies.isEmpty()) {
            return new AcademyCheckResponse(false, null);
        }

        boolean isApproved = academies.stream()
                .anyMatch(a -> a.getApprovalStatus() == ApprovalStatus.APPROVED);

        Long academyId = academies.get(academies.size() - 1).getId();

        return new AcademyCheckResponse(isApproved, academyId);
    }

    /**
     * 신규 학원 등록 로직
     * 1. 학원 정보 저장 -> 2. 폴더 생성 -> 3. 파일 저장 -> 4. 이미지/연관 데이터 매핑
     */
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

    /**
     * 대시보드 통계 데이터 집계
     * 여러 Repository를 조회하여 현재 운영 현황 수치를 산출함
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats(Long academyId) {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = today.withDayOfMonth(today.lengthOfMonth()).atTime(23, 59, 59);
        String dayOfWeek = today.getDayOfWeek().name().toLowerCase();

        List<AttendanceEntity.AttendanceStatus> activeStatuses = List.of(
                AttendanceEntity.AttendanceStatus.PRESENT,
                AttendanceEntity.AttendanceStatus.LATE,
                AttendanceEntity.AttendanceStatus.EARLY_LEAVE
        );

        Long studentCount = classInfoStudentRepository.countUniqueStudentsByAcademy(academyId, StudentStatus.ENROLLED);
        Long totalTodayCount = classInfoStudentRepository.countExpectedStudentsToday(
                academyId,
                dayOfWeek,
                StudentStatus.ENROLLED
        );
        Long presentCount = attendanceRepository.countByAcademyAndDateAndStatusIn(
                academyId,
                today,
                activeStatuses
        );
        Long noCardCount = academyStudentRepository.countStudentsWithoutCard(academyId);
        Long totalMonthlyFee = receiptRepository.sumAmountByAcademyAndPeriod(academyId, startOfMonth, endOfMonth);

        return DashboardStatsResponse.builder()
                .studentCount(studentCount)
                .presentCount(presentCount)
                .totalTodayCount(totalTodayCount)
                .noCardCount(noCardCount)
                .totalMonthlyFee(totalMonthlyFee != null ? totalMonthlyFee : 0L)
                .build();
    }

    @Transactional(readOnly = true)
    public List<AcademyListResponse> getMyAcademies(Long userId) {
        return academyRepository.findAllByUserId(userId).stream()
                .map(academy -> AcademyListResponse.builder()
                        .academyId(academy.getId())
                        .name(academy.getName())
                        .approvalStatus(academy.getApprovalStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AcademyResponse getAcademyDetail(Long id) {
        AcademyEntity academy = academyRepository.findDetailById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        return AcademyResponse.from(academy);
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> getAcademySubjects(Long academyId) {
        List<AcademySubjectEntity> mappings = academySubjectRepository.findAllByAcademyId(academyId);

        return mappings.stream()
                .map(mapping -> mapping.getSubject().toResponseDto())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgeResponse> getAcademyAges(Long academyId) {
        List<AcademyAgeEntity> mappings = academyAgeRepository.findAllByAcademyId(academyId);

        return mappings.stream()
                .map(mapping -> mapping.getAge().toResponseDto())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateAcademy(Long id, AcademyUpdateRequest req, Long userId) {
        AcademyEntity academy = academyRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        if (!academy.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.NO_AUTHORITY);
        }

        academy.update(req);
        updateRelationalData(academy, req);

        String academyFolderPath = IMAGE_SAVE_PATH + academy.getId() + "/";

        List<AcademyImageEntity> oldImages = academy.getAcademyImages();
        List<AcademyImageEntity> imagesToRemove = new ArrayList<>();

        for (AcademyImageEntity imageEntity : oldImages) {
            String oldUrl = imageEntity.getImageUrl();
            if (req.getImageUrls() == null || !req.getImageUrls().contains(oldUrl)) {
                deletePhysicalFile(oldUrl);
                imagesToRemove.add(imageEntity);
            }
        }

        oldImages.removeAll(imagesToRemove);

        if (req.getImageFiles() != null && !req.getImageFiles().isEmpty()) {
            File folder = new File(academyFolderPath);
            if (!folder.exists()) folder.mkdirs();

            for (MultipartFile file : req.getImageFiles()) {
                if (file.isEmpty()) continue;

                String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
                try {
                    file.transferTo(new File(academyFolderPath + filename));
                    String newUrl = IMAGE_BASE_URL + academy.getId() + "/" + filename;

                    AcademyImageEntity newImage = AcademyImageEntity.builder()
                            .imageUrl(newUrl)
                            .academy(academy)
                            .build();
                    oldImages.add(newImage);
                } catch (IOException e) {
                    throw new RuntimeException("새 이미지 저장 실패", e);
                }
            }
        }
    }

    private void deletePhysicalFile(String fileUrl) {
        try {
            String relativePath = fileUrl.replace(IMAGE_BASE_URL, "");
            File fileToDelete = new File(IMAGE_SAVE_PATH + relativePath);

            if (fileToDelete.exists()) {
                fileToDelete.delete();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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
    public PageResponseDto<AcademySearchResponse> getPendingAcademies(Pageable pageable) {
        Page<AcademyEntity> pendingPage = academyRepository.findAllByApprovalStatus(ApprovalStatus.PENDING, pageable);

        Page<AcademySearchResponse> dtoPage = pendingPage.map(AcademySearchResponse::from);

        return new PageResponseDto<>(dtoPage);
    }

    private void updateRelationalData(AcademyEntity academy, AcademyRequest req) {
        if (req.getAgeIds() != null) {
            academy.updateAges(ageRepository.findAllById(req.getAgeIds()));
        }

        if (req.getSubjects() != null) {
            academy.updateSubjects(subjectRepository.findAllById(req.getSubjects()));
        }

        if (req instanceof AcademyUpdateRequest updateDto) {
            if (updateDto.getImageUrls() != null) {
                academy.updateImages(updateDto.getImageUrls());
            }
        }
    }

}

