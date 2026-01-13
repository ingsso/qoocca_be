package com.example.qoocca_be.classInfo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ClassInfoStudentRequestDTO {

    @Schema(description = "등록할 기존 학생 ID", example = "1", required = true)
    private Long studentId;
}
