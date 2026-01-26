package com.qoocca.teachers.api.academy.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AcademyStudentUploadError {
    private int rowNumber;
    private String message;
}
