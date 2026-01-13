package com.example.qoocca_be.student.model;

import com.example.qoocca_be.classInfo.entity.StudentStatus;
import com.example.qoocca_be.student.entity.StudentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StudentResponse {

    private Long studentId;
    private String studentName;

    public static StudentResponse from(StudentEntity entity) {
        return StudentResponse.builder()
                .studentId(entity.getStudentId())
                .studentName(entity.getStudentName())
                .build();
    }
}

