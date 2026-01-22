package com.qoocca.teachers.user.model;

public record SocialLinkRequest(
        String phone,
        String socialId,
        String provider,
        UserRequest.AgreementsRequest agreements
) {}
