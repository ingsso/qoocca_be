package com.qoocca.teachers.api.classInfo.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class ClassStudentRequest {

    @Schema(description = "등록할 기존 학생 ID", example = "1")
    private Long studentId;
}
