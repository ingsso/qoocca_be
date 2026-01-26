package com.qoocca.teachers.api.student.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class StudentCreateRequest {
    @NotBlank
    private String studentName;

    @NotBlank
    private String studentPhone;

}
