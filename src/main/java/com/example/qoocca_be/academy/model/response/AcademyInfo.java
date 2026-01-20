package com.example.qoocca_be.academy.model.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AcademyInfo {
    private Long id;
    private String name;
    private String approvalStatus;
}