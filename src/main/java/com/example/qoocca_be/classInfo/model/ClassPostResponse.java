package com.example.qoocca_be.classInfo.model;

import com.example.qoocca_be.classInfo.entity.ClassInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassPostResponse {

    private Long classId;
    private String className;

    public static ClassPostResponse fromEntity(ClassInfoEntity entity) {
        return ClassPostResponse.builder()
                .classId(entity.getClassId())
                .className(entity.getClassName())
                .build();
    }
}
