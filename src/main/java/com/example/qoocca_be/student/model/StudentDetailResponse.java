package com.example.qoocca_be.student.model;


import com.example.qoocca_be.student.entity.StudentEntity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class StudentDetailResponse {

    private Long studentId;
    private String studentName;
    private StudentEntity.StudentStatus studentStatus;
    private Long classId;
    private LocalDateTime createdAt;

    public static StudentDetailResponse from(StudentEntity entity) {
        return StudentDetailResponse.builder()
                .studentId(entity.getStudentId())
                .studentName(entity.getStudentName())
                .studentStatus(entity.getStudentStatus())
                .classId(entity.getClassInfo().getClassId())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

