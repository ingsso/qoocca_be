package com.qoocca.teachers.api.classInfo.model.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalTime;

@Data
public class ClassCreateRequest {

    @Schema(description = "수업명", example = "Alpha 교과국어 초등반")
    private String className;

    @Schema(
            description = "수업 시작 시간 (HH:mm)",
            example = "14:00",
            type = "string"
    )
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm")
    private LocalTime startTime;

    @Schema(
            description = "수업 종료 시간 (HH:mm)",
            example = "16:00",
            type = "string"
    )
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

    @Schema(description = "수업 가격 (원)", example = "600000")
    private Long price;

    @Schema(description = "연령 ID (age 테이블 PK)", example = "2")
    private Long ageId;

    @Schema(description = "과목 ID (subject 테이블 PK)", example = "70")
    private Long subjectId;
}
