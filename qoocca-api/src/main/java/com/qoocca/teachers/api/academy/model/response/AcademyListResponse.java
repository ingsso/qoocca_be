package com.qoocca.teachers.api.academy.model.response;

import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AcademyListResponse {
    private Long academyId;
    private String name;
    private ApprovalStatus approvalStatus; // PENDING, APPROVED, REJECTED

    public static AcademyListResponse from(AcademyEntity entity) {
        return AcademyListResponse.builder()
                .academyId(entity.getId())
                .name(entity.getName())
                .approvalStatus(entity.getApprovalStatus())
                .build();
    }
}