package com.qoocca.teachers.academy.model.response;

public record AcademyCheckResponse(
        boolean isApproved,
        Long academyId
) {}