package com.qoocca.teachers.api.parent.model;

import com.qoocca.teachers.db.parent.entity.ParentEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentResponse {

    private Long parentId;
    private String parentName;
    private String cardNum;
    private Boolean cardState;
    private String parentRelationship;
    private String parentPhone;
    private Boolean isPay;
    private Boolean alarm;

    public static ParentResponse from(ParentEntity entity) {
        return ParentResponse.builder()
                .parentId(entity.getParentId())
                .parentName(entity.getParentName())
                .cardNum(entity.getCardNum())
                .cardState(entity.getCardState())
                .parentRelationship(entity.getParentRelationship())
                .parentPhone(entity.getParentPhone())
                .isPay(entity.getIsPay())
                .alarm(entity.getAlarm())
                .build();
    }
}
