package com.example.qoocca_be.parent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentUpdateRequest {

    private String parentName;   // ✅ 추가
    private String cardNum;
    private Boolean cardState;
    private String parentRelationship;
    private String parentPhone;
    private Boolean isPay;
    private Boolean alarm;
}
