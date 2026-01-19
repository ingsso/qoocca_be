package com.example.qoocca_be.academy.model.response;

import com.example.qoocca_be.academy.entity.AcademyEntity;

public record AcademySearchResponse(String name, String address) {
    public static AcademySearchResponse from(AcademyEntity academy) {
        return new AcademySearchResponse(
                academy.getName(),
                academy.getAddress()
        );
    }
}

