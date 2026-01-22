package com.qoocca.teachers.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
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

        public boolean isService() { return service; }
        public boolean isPrivacy() { return privacy; }
        public boolean isThirdParty() { return thirdParty; }
        public boolean isMarketing() { return marketing; }
    }
}