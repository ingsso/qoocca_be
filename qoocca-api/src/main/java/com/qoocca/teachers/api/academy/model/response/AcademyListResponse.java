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
    private String rejectionReason;
    private String userName;
    private String userPhoneNumber;

    public static AcademyListResponse from(AcademyEntity entity) {
        return AcademyListResponse.builder()
                .academyId(entity.getId())
                .name(entity.getName())
                .approvalStatus(entity.getApprovalStatus())
                .rejectionReason(entity.getRejectionReason())
                .userName(entity.getUser() != null ? entity.getUser().getUserName() : null)
                .userPhoneNumber(entity.getUser() != null ? entity.getUser().getPhoneNumber() : null)
                .build();
    }
}
