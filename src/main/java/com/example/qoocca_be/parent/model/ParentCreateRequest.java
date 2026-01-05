package com.example.qoocca_be.parent.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ParentCreateRequest {

    @NotBlank
    private String cardNum;

    @NotNull
    private Boolean cardState;

    @NotBlank
    private String parentRelationship;

    @NotBlank
    private String parentPhone;

    @NotNull
    private Boolean isPay;

    @NotNull
    private Boolean alarm;
}
