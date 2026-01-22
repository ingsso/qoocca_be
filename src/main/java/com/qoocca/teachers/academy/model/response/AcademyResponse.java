package com.qoocca.teachers.academy.model.response;

import com.qoocca.teachers.academy.entity.AcademyEntity;
import com.qoocca.teachers.age.model.AgeResponse;
import com.qoocca.teachers.subject.model.SubjectResponse;
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
                .imageUrls(academy.getAcademyImages().stream()
                        .map(image -> image.getImageUrl())
                        .collect(Collectors.toList()))
                .ages(academy.getAcademyAges().stream()
                        .map(aa -> aa.getAge().toResponseDto())
                        .collect(Collectors.toList()))
                .subjects(academy.getAcademySubjects().stream()
                        .map(as -> as.getSubject().toResponseDto())
                        .collect(Collectors.toList()))
                .build();
    }
}
