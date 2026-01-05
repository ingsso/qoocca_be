package com.qoocca.be.academy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AcademyRequestDto {

    @NotBlank(message = "학원 이름은 필수 입니다.")
    private String name;

    @NotBlank(message = "기본 주소는 필수입니다.")
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

    @NotBlank(message = "사업자 등록증은 필수입니다.")
    private String certificate;
}
