package com.example.qoocca_be.student.model;

import com.example.qoocca_be.student.entity.StudentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentCreateResponse {

    private Long studentId;
    private String studentName;
    private StudentEntity.StudentStatus studentStatus;

    public static StudentCreateResponse from(StudentEntity entity) {
        return StudentCreateResponse.builder()
                .studentId(entity.getStudentId())
                .studentName(entity.getStudentName())
                .studentStatus(entity.getStudentStatus())
                .build();
    }
}

