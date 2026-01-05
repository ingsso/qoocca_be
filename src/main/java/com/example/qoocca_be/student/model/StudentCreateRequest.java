package com.example.qoocca_be.student.model;

import com.example.qoocca_be.student.entity.StudentEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class StudentCreateRequest {
    @NotBlank
    private String studentName;

    private StudentEntity.StudentStatus studentStatus;
}
