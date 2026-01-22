package com.qoocca.teachers.academy.model.response;

import com.qoocca.teachers.academy.entity.ApprovalStatus;
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
}