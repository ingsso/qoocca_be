package com.example.qoocca_be.academy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AcademyStudentCreateRequest {

    @NotBlank
    private String studentName;
}
