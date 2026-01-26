package com.qoocca.teachers.api.academy.model.response;

public record AcademyCheckResponse(
        boolean isApproved,
        Long academyId
) {}