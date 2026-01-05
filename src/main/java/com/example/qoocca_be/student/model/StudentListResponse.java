package com.example.qoocca_be.student.model;

import com.example.qoocca_be.student.entity.StudentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentListResponse {

    private Long studentId;
    private String studentName;
    private StudentEntity.StudentStatus studentStatus;

    public static StudentListResponse from(StudentEntity entity) {
        return StudentListResponse.builder()
                .studentId(entity.getStudentId())
                .studentName(entity.getStudentName())
                .studentStatus(entity.getStudentStatus())
                .build();
    }
}
