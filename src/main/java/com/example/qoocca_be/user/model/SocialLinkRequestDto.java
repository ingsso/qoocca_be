package com.example.qoocca_be.user.model;

public record SocialLinkRequestDto(
        String phone,
        String socialId,
        String provider,
        UserRequestDto.AgreementsRequest agreements
) {}
