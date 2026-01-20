package com.example.qoocca_be.academy.model.response;

import com.example.qoocca_be.academy.entity.ApprovalStatus;
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