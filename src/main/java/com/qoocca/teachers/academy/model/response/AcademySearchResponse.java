package com.qoocca.teachers.academy.model.response;

import com.qoocca.teachers.academy.entity.AcademyEntity;

public record AcademySearchResponse(String name, String address) {
    public static AcademySearchResponse from(AcademyEntity academy) {
        return new AcademySearchResponse(
                academy.getName(),
                academy.getAddress()
        );
    }
}

