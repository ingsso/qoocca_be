package com.qoocca.teachers.api.academy.model.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
@Builder
public class AcademyStudentUploadResponse {
    private int totalRows;
    private int successCount;
    private int failureCount;
    private Map<String, String> headerMapping;
    private List<AcademyStudentUploadError> errors;
}
