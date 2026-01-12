package com.example.qoocca_be.user.model;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponseDto {
    private String accessToken;
    private Long academyId;
    private String refreshToken;
    private String socialId;
}
