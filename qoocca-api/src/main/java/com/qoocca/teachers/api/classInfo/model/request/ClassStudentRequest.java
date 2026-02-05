package com.qoocca.teachers.api.classInfo.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ClassStudentRequest {

    @NotNull
    @Schema(description = "등록할 기존 학생 ID", example = "1")
    private Long studentId;

    @Schema(description = "클래스 결제자 보호자 ID (미지정 시 첫 등록 보호자 fallback)", example = "456")
    private Long payerId;
}
