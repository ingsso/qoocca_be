package com.qoocca.teachers.api.classInfo.model.response;

import com.qoocca.teachers.db.classInfo.model.ClassSummaryDto;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSummaryResponse {
    private Long classId;
    private String className;
    private String classTime;
    private Integer currentCount;
    private Long presentCount;
    private Long lateCount;
    private Long absentCount;
    private Integer notPresentCount;

    public static ClassSummaryResponse from(ClassSummaryDto dto) {
        return ClassSummaryResponse.builder()
                .classId(dto.getClassId())
                .className(dto.getClassName())
                .currentCount(dto.getCurrentCount())
                .presentCount(dto.getPresentCount())
                .lateCount(dto.getLateCount())
                .absentCount(dto.getAbsentCount())
                .notPresentCount(dto.getCurrentCount() - (int)(dto.getPresentCount() + dto.getLateCount() + dto.getAbsentCount()))
                .build();
    }
}