package com.qoocca.teachers.api.academy.model.response;

import java.time.LocalDateTime;

public record AcademyStudentUploadEnqueueResponse(
        String jobId,
        String status,
        LocalDateTime submittedAt
) {
}
