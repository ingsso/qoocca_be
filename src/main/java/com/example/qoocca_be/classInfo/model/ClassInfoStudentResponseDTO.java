package com.example.qoocca_be.classInfo.model;

import com.example.qoocca_be.classInfo.entity.ClassInfoStudentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassInfoStudentResponseDTO {

    private Long studentId;
    private String studentName;

    public static ClassInfoStudentResponseDTO from(ClassInfoStudentEntity entity) {
        return ClassInfoStudentResponseDTO.builder()
                .studentId(entity.getStudent().getStudentId())
                .studentName(entity.getStudent().getStudentName())
                .build();
    }
}
