package com.example.qoocca_be.classInfo.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassStatsResponseDTO {

    private Long classId;
    private String className;
    private Long totalStudents;
    private Long withdrawnStudents;

    public static ClassStatsResponseDTO from(ClassStatsProjection p) {
        return ClassStatsResponseDTO.builder()
                .classId(p.getClassId())
                .className(p.getClassName())
                .totalStudents(p.getTotalStudents())
                .withdrawnStudents(p.getWithdrawnStudents())
                .build();
    }
}
