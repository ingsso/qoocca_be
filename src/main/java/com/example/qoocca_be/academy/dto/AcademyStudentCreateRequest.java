package com.example.qoocca_be.academy.dto;

import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.student.entity.StudentEntity;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class AcademyStudentCreateRequest {

    @NotBlank
    private String studentName;
    private StudentStatus studentStatus;
}
