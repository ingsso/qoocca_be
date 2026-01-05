package com.example.qoocca_be.parent.model;

import com.example.qoocca_be.parent.entity.ParentEntity;
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
    private String cardNum;
    private Boolean cardState;
    private String parentRelationship;
    private String parentPhone;
    private Boolean isPay;
    private Boolean alarm;

    public static ParentResponse from(ParentEntity entity) {
        return ParentResponse.builder()
                .parentId(entity.getParentId())
                .cardNum(entity.getCardNum())
                .cardState(entity.getCardState())
                .parentRelationship(entity.getParentRelationship())
                .parentPhone(entity.getParentPhone())
                .isPay(entity.getIsPay())
                .alarm(entity.getAlarm())
                .build();
    }
}
