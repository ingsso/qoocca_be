package com.qoocca.teachers.db.classInfo.model;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClassSummaryDto {
    private Long classId;
    private String className;
    private Integer currentCount;
    private Long presentCount;
    private Long lateCount;
    private Long absentCount;
}