package com.qoocca.teachers.api.classInfo.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ClassCreateRequest {

    @NotBlank
    @Schema(description = "반 이름", example = "알파 교과국어 초등반")
    private String className;

    @NotNull
    @Schema(description = "수업 시작 시간 (HH:mm)", example = "14:00", type = "string")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull
    @Schema(description = "수업 종료 시간 (HH:mm)", example = "16:00", type = "string")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime endTime;

    @Schema(description = "월요일 수업 여부", example = "true")
    private boolean monday;

    @Schema(description = "화요일 수업 여부", example = "false")
    private boolean tuesday;

    @Schema(description = "수요일 수업 여부", example = "false")
    private boolean wednesday;

    @Schema(description = "목요일 수업 여부", example = "false")
    private boolean thursday;

    @Schema(description = "금요일 수업 여부", example = "true")
    private boolean friday;

    @Schema(description = "토요일 수업 여부", example = "false")
    private boolean saturday;

    @Schema(description = "일요일 수업 여부", example = "false")
    private boolean sunday;

    @PositiveOrZero
    @Schema(description = "수업 금액", example = "600000")
    private Long price;

    @NotNull
    @Schema(description = "연령 ID", example = "2")
    private Long ageId;

    @NotNull
    @Schema(description = "과목 ID", example = "70")
    private Long subjectId;

    @AssertTrue(message = "최소 1개 이상의 수업 요일을 선택해야 합니다.")
    public boolean isAnyClassDaySelected() {
        return monday || tuesday || wednesday || thursday || friday || saturday || sunday;
    }
}
