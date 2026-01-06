package com.example.qoocca_be.user.model;

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

    private AgreementsRequest agreements;

    @Getter
    @Setter
    public static class AgreementsRequest {
        private boolean service;
        private boolean privacy;
        private boolean thirdParty;
        private boolean marketing;

        public boolean isAllRequiredAgreed() {
            return service && privacy && thirdParty;
        }
    }
}