package com.example.qoocca_be.user.model;

import jakarta.validation.constraints.AssertTrue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    private String username;
    private String email;
    private String password;
    private String phone;

    @AssertTrue(message = "서비스 이용을 위해 약관 동의가 필수입니다.")
    private Boolean agree;
}