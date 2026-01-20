package com.example.qoocca_be.user.model;

import com.example.qoocca_be.academy.model.response.AcademyInfo;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String accessToken;
    private Long academyId;              // 단일 학원용
    private List<AcademyInfo> academies; // 다중 학원용
    private String refreshToken;
    private String socialId;
}
