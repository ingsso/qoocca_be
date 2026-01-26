package com.qoocca.teachers.api.user.model;

public record SocialLinkRequest(
        String phone,
        String socialId,
        String provider,
        UserRequest.AgreementsRequest agreements
) {}
