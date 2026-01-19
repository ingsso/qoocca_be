package com.example.qoocca_be.classInfo.model.response;

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

    public ClassSummaryResponse(Long classId, String className, int currentCount,
                                Long presentCount, Long lateCount, Long absentCount) {
        this.classId = classId;
        this.className = className;
        this.currentCount = currentCount;
        this.presentCount = presentCount != null ? presentCount : 0L;
        this.lateCount = lateCount != null ? lateCount : 0L;
        this.absentCount = absentCount != null ? absentCount : 0L;
        this.notPresentCount = currentCount - (int)(this.presentCount + this.lateCount + this.absentCount);
    }
}