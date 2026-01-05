package com.example.qoocca_be.academy.dto;

import com.example.qoocca_be.academy.entity.AcademyEntity;
import com.example.qoocca_be.age.model.AgeResponseDto;
import com.example.qoocca_be.subject.model.SubjectResponseDto;
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
public class AcademyResponseDto {

    private Long id;

    private String name;

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
    private List<AgeResponseDto> ages;
    private List<SubjectResponseDto> subjects;

    public static AcademyResponseDto from(AcademyEntity academy) {
        return AcademyResponseDto.builder()
                .id(academy.getId())
                .name(academy.getName())
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
                .imageUrls(academy.getImages().stream()
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
