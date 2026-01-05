package com.qoocca.be.academy.dto;

import com.qoocca.be.academy.entity.AcademyEntity;

public record AcademySearchResponseDto(String name, String address) {
    public static AcademySearchResponseDto from(AcademyEntity academy) {
        return new AcademySearchResponseDto(
                academy.getName(),
                academy.getAddress()
        );
    }
}

