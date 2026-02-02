package com.qoocca.teachers.api.parent.auth.dto;

import com.qoocca.teachers.db.parent.entity.ParentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentLoginResponse {

    private Long parentId;
    private String parentName;
    private String parentPhone;
    private String accessToken;

    public static ParentLoginResponse from(ParentEntity parent, String accessToken) {
        return ParentLoginResponse.builder()
                .parentId(parent.getParentId())
                .parentName(parent.getParentName())
                .parentPhone(parent.getParentPhone())
                .accessToken(accessToken)
                .build();
    }
}
