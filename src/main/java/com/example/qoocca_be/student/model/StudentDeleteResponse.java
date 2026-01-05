package com.example.qoocca_be.student.model;

import com.example.qoocca_be.student.entity.StudentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentDeleteResponse {

    private Long studentId;
    private StudentEntity.StudentStatus studentStatus;

    public static StudentDeleteResponse from(StudentEntity entity) {
        return StudentDeleteResponse.builder()
                .studentId(entity.getStudentId())
                .studentStatus(entity.getStudentStatus())
                .build();
    }
}
