package com.example.qoocca_be.academy.model.response;

public record AcademyCheckResponse(
        boolean isApproved,
        Long academyId
) {}