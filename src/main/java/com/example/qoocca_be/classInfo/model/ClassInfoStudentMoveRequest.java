package com.example.qoocca_be.classInfo.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ClassInfoStudentMoveRequest {

    @NotNull
    @Schema(description = "이동할 대상 반 ID", example = "5")
    private Long targetClassId;
}
