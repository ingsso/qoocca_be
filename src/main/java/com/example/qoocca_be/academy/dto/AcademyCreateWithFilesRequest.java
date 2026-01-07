package com.example.qoocca_be.academy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademyCreateWithFilesRequest {

    @NotBlank(message = "학원 이름은 필수입니다.")
    private String name;

    @NotBlank(message = "기본 주소는 필수입니다.")
    private String baseAddress;

    private String detailAddress;
    private String briefInfo;
    private List<Long> ageIds;
    private List<Long> subjects;
    private String detailInfo;
    private String phoneNumber;

    private String blogUrl;
    private String websiteUrl;
    private String instagramUrl;

    @NotBlank(message = "사업자 등록증은 필수입니다.")
    private String certificate;

    // ⭐ 여기서 MultipartFile로 이미지 파일 받기
    private List<MultipartFile> imageFiles;
}
