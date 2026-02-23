package com.qoocca.teachers.api.academy.model.response;

import java.time.LocalDateTime;

public record AcademyImageUploadEnqueueResponse(
        String jobId,
        String status,
        LocalDateTime submittedAt
) {
}
