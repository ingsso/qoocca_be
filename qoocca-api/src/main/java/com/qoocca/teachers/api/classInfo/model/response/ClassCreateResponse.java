package com.qoocca.teachers.api.classInfo.model.response;

import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassCreateResponse {

    private Long classId;
    private String className;

    public static ClassCreateResponse fromEntity(ClassInfoEntity entity) {
        return ClassCreateResponse.builder()
                .classId(entity.getClassId())
                .className(entity.getClassName())
                .build();
    }
}
