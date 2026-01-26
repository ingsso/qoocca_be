package com.qoocca.teachers.api.age.model;

import com.qoocca.teachers.db.age.entity.AgeEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AgeResponse {
    private Long id;
    private String ageCode;

    public static AgeResponse from(AgeEntity entity) {
        return AgeResponse.builder()
                .id(entity.getId())
                .ageCode(entity.getAgeCode())
                .build();
    }
}
