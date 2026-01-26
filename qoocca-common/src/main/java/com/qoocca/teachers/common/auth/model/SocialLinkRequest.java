package com.qoocca.teachers.common.auth.model;

import com.qoocca.teachers.common.auth.model.UserRequest.AgreementsRequest;

public record SocialLinkRequest(
        String provider,
        String socialId,
        String phone,
        AgreementsRequest agreements
) {}
