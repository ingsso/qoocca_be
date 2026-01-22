package com.qoocca.teachers.classInfo.model.response;

import com.qoocca.teachers.classInfo.model.ClassStatsProjection;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassStatsResponse {

    private Long classId;
    private String className;
    private Long totalStudents;
    private Long withdrawnStudents;

    public static ClassStatsResponse from(ClassStatsProjection p) {
        return ClassStatsResponse.builder()
                .classId(p.getClassId())
                .className(p.getClassName())
                .totalStudents(p.getTotalStudents())
                .withdrawnStudents(p.getWithdrawnStudents())
                .build();
    }
}
