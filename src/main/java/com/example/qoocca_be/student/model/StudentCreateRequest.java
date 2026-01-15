package com.example.qoocca_be.student.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class StudentCreateRequest {
    @NotBlank
    private String studentName;

}
