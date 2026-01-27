package com.qoocca.teachers.api.academy.model.response;

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
public class AcademyResponse {

    private Long id;

    private String name;

    private String approvalStatus;

    private String address;
    private String baseAddress;
    private String detailAddress;

    private String briefInfo;

    private String detailInfo;
    private String costInfo;

    private String operatingHours;

    private String phoneNumber;

    private String blogUrl;
    private String websiteUrl;
    private String instagramUrl;

    private String certificate;
    private String rejectionReason;

    private List<String> imageUrls;
    private List<AgeResponse> ages;
    private List<SubjectResponse> subjects;

    public static AcademyResponse from(AcademyEntity academy) {
        return AcademyResponse.builder()
                .id(academy.getId())
                .name(academy.getName())
                .approvalStatus(academy.getApprovalStatus().name())
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
                .certificate(academy.getCertificate())
                .rejectionReason(academy.getRejectionReason())
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
