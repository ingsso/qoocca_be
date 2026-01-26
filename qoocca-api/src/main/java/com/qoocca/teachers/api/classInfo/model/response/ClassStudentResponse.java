package com.qoocca.teachers.api.classInfo.model.response;

import com.qoocca.teachers.db.classInfo.entity.ClassInfoStudentEntity;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ClassStudentResponse {

    private Long studentId;
    private String studentName;

    public static ClassStudentResponse from(ClassInfoStudentEntity entity) {
        return ClassStudentResponse.builder()
                .studentId(entity.getStudent().getStudentId())
                .studentName(entity.getStudent().getStudentName())
                .build();
    }
}
