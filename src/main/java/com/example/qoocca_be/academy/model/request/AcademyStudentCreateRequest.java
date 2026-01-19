package com.example.qoocca_be.academy.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AcademyStudentCreateRequest {

    @NotBlank
    private String studentName;

    // ✅ 추가
    @NotBlank
    private String studentPhone;
}
