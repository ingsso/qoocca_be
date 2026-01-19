package com.example.qoocca_be.classInfo.model.request;

import com.example.qoocca_be.classInfo.entity.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ClassStudentModifyRequest {

    @NotNull
    @Schema(description = "학생 수강 상태", example = "ENROLLED")
    private StudentStatus status;
}
