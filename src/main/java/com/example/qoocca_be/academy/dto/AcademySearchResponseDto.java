package com.example.qoocca_be.academy.dto;

import com.example.qoocca_be.academy.entity.AcademyEntity;

public record AcademySearchResponseDto(String name, String address) {
    public static AcademySearchResponseDto from(AcademyEntity academy) {
        return new AcademySearchResponseDto(
                academy.getName(),
                academy.getAddress()
        );
    }
}

