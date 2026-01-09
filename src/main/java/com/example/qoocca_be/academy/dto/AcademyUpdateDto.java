package com.example.qoocca_be.academy.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademyUpdateDto implements AcademyRequest {

    private String name;

    private String baseAddress;
    private String detailAddress;

    private String briefInfo;

    private List<Long> ageIds;
    private List<Long> subjects;

    private String detailInfo;
    private String costInfo;

    private String operatingHours;

    private String phoneNumber;

    private String blogUrl;
    private String websiteUrl;
    private String instagramUrl;

    private List<String> imageUrls;

    private List<MultipartFile> imageFiles;

    private MultipartFile certificateFile;

    private String certificate;
}
