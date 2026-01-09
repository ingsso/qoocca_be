package com.example.qoocca_be.academy.dto;

public record AcademyCheckResponseDto (
        boolean isApproved,
        Long academyId
) {}