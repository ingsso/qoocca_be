package com.qoocca.teachers.api.admin.model.response;

import com.qoocca.teachers.api.age.model.AgeResponse;
import com.qoocca.teachers.api.subject.model.SubjectResponse;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.AcademyImageEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AdminAcademyDetailResponse {

    private Long id;
    private String name;
    private String approvalStatus;
    private String rejectionReason;
    private String certificate;

    private String address;
    private String baseAddress;
    private String detailAddress;

    private String phoneNumber;
    private String briefInfo;
    private String detailInfo;
    private String costInfo;
    private String operatingHours;

    private String blogUrl;
    private String websiteUrl;
    private String instagramUrl;

    private String userName;
    private String userPhoneNumber;

    private List<String> imageUrls;
    private List<AgeResponse> ages;
    private List<SubjectResponse> subjects;

    public static AdminAcademyDetailResponse from(AcademyEntity academy) {
        return AdminAcademyDetailResponse.builder()
                .id(academy.getId())
                .name(academy.getName())
                .approvalStatus(academy.getApprovalStatus().name())
                .rejectionReason(academy.getRejectionReason())
                .certificate(academy.getCertificate())
                .address(academy.getAddress())
                .baseAddress(academy.getBaseAddress())
                .detailAddress(academy.getDetailAddress())
                .phoneNumber(academy.getPhoneNumber())
                .briefInfo(academy.getBriefInfo())
                .detailInfo(academy.getDetailInfo())
                .costInfo(academy.getCostInfo())
                .operatingHours(academy.getOperatingHours())
                .blogUrl(academy.getBlogUrl())
                .websiteUrl(academy.getWebsiteUrl())
                .instagramUrl(academy.getInstagramUrl())
                .userName(academy.getUser() != null ? academy.getUser().getUserName() : null)
                .userPhoneNumber(academy.getUser() != null ? academy.getUser().getPhoneNumber() : null)
                .imageUrls(academy.getAcademyImages().stream()
                        .map(AcademyImageEntity::getImageUrl)
                        .collect(Collectors.toList()))
                .ages(academy.getAcademyAges().stream()
                        .map(aa -> AgeResponse.from(aa.getAge()))
                        .collect(Collectors.toList()))
                .subjects(academy.getAcademySubjects().stream()
                        .map(as -> SubjectResponse.from(as.getSubject()))
                        .collect(Collectors.toList()))
                .build();
    }
}
