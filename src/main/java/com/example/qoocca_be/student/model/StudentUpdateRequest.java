package com.example.qoocca_be.student.model;

import com.example.qoocca_be.student.entity.StudentEntity;
import lombok.Getter;

@Getter
public class StudentUpdateRequest {
    private String studentName;
    private StudentEntity.StudentStatus studentStatus;
}
