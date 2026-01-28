package com.qoocca.teachers.api.parent.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentUpdateRequest {

    private String parentName;
    private String cardNum;
    private String parentRelationship;
    private String parentPhone;
    private Boolean isPay;
    private Boolean alarm;
}
