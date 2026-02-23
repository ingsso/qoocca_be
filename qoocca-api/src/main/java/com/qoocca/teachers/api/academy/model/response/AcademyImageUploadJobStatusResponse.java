package com.qoocca.teachers.api.academy.model.response;

import java.time.LocalDateTime;

public record AcademyImageUploadJobStatusResponse(
        String jobId,
        Long academyId,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime completedAt,
        String errorMessage
) {
}
