package com.example.qoocca_be.age.model;

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
}
