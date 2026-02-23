package com.qoocca.teachers.api.academy.model.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record AcademyStudentUploadJobStatusResponse(
        String jobId,
        Long academyId,
        String status,
        LocalDateTime submittedAt,
        LocalDateTime completedAt,
        Integer totalRows,
        Integer successCount,
        Integer failureCount,
        Map<String, String> headerMapping,
        List<AcademyStudentUploadError> errors,
        String errorMessage
) {
}
