package com.qoocca.teachers.api.academy.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AcademyResubmitRequest {

    private String name;
    private String baseAddress;
    private String detailAddress;
    private MultipartFile certificateFile;
}
