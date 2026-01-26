package com.qoocca.teachers.api.academy.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AcademyStudentCreateRequest {

    @NotBlank
    private String studentName;

    @NotBlank
    private String studentPhone;
}
