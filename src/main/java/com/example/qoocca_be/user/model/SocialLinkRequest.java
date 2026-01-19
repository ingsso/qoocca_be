package com.example.qoocca_be.user.model;

public record SocialLinkRequest(
        String phone,
        String socialId,
        String provider,
        UserRequest.AgreementsRequest agreements
) {}
