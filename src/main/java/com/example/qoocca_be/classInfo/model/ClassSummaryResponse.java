package com.example.qoocca_be.classInfo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassSummaryResponse {
    private Long classId;
    private String className;
    private Integer currentCount;
    private Long presentCount;
    private Long lateCount;
    private Long absentCount;
}