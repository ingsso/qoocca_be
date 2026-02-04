package com.qoocca.teachers.api.academy.service;

import com.qoocca.teachers.api.academy.model.response.AcademyListResponse;
import com.qoocca.teachers.api.admin.model.response.AdminAcademyDetailResponse;
import com.qoocca.teachers.common.global.common.PageResponse;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.ApprovalStatus;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AcademyApprovalService {

    private final AcademyRepository academyRepository;

    @Transactional
    public void approveAcademy(Long academyId) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        academy.updateApprovalStatus(ApprovalStatus.APPROVED);
        academy.setRejectionReason(null);
    }

    @Transactional
    public void rejectAcademy(Long academyId, String rejectionReason) {
        AcademyEntity academy = academyRepository.findById(academyId)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        academy.updateApprovalStatus(ApprovalStatus.REJECTED);
        academy.setRejectionReason(rejectionReason);
    }

    @Transactional(readOnly = true)
    public PageResponse<AcademyListResponse> getPendingAcademies(Pageable pageable) {
        Page<AcademyEntity> pendingPage = academyRepository.findAllByApprovalStatus(ApprovalStatus.PENDING, pageable);
        Page<AcademyListResponse> dtoPage = pendingPage.map(AcademyListResponse::from);
        return new PageResponse<>(dtoPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<AcademyListResponse> getRejectedAcademies(Pageable pageable) {
        Page<AcademyEntity> rejectedPage = academyRepository.findAllByApprovalStatus(ApprovalStatus.REJECTED, pageable);
        Page<AcademyListResponse> dtoPage = rejectedPage.map(AcademyListResponse::from);
        return new PageResponse<>(dtoPage);
    }

    @Transactional(readOnly = true)
    public PageResponse<AcademyListResponse> getAllAcademies(Pageable pageable) {
        Page<AcademyEntity> academyPage = academyRepository.findAll(pageable);
        Page<AcademyListResponse> dtoPage = academyPage.map(AcademyListResponse::from);
        return new PageResponse<>(dtoPage);
    }

    @Transactional(readOnly = true)
    public AdminAcademyDetailResponse getAdminAcademyDetail(Long id) {
        AcademyEntity academy = academyRepository.findAdminDetailById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));

        return AdminAcademyDetailResponse.from(academy);
    }
}