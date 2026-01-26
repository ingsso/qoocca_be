package com.qoocca.teachers.api.user.service;

import com.qoocca.teachers.api.global.jwt.JwtTokenProvider;
import com.qoocca.teachers.common.global.utils.CookieUtils;
import com.qoocca.teachers.db.user.entity.OauthProvider;
import com.qoocca.teachers.db.user.entity.UserEntity;
import com.qoocca.teachers.api.user.model.LoginResponse;
import com.qoocca.teachers.db.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
public abstract class SocialOauthService {

    protected final JwtTokenProvider jwtTokenProvider;
    protected final UserRepository userRepository;
    protected final RestTemplate restTemplate;
    protected final CookieUtils cookieUtils;

    protected SocialOauthService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository,
                                 RestTemplate restTemplate, CookieUtils cookieUtils) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.cookieUtils = cookieUtils;
    }

    public abstract OauthProvider getProvider();
    public abstract LoginResponse login(String code, HttpServletResponse response);

    protected LoginResponse processSocialLogin(String socialId, HttpServletResponse response) {
        Optional<UserEntity> userOpt = findUserBySocialId(socialId);

        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            if (user.getPhoneNumber() == null) {
                return needPhoneAuthResponse(socialId);
            }
            return generateLoginResponse(user, response);
        } else {
            saveNewSocialUser(socialId);
            return needPhoneAuthResponse(socialId);
        }
    }

    private LoginResponse generateLoginResponse(UserEntity user, HttpServletResponse res) {
        LoginResponse loginRes = jwtTokenProvider.generateTokens(user.getId(), user.getRole(), res);
        cookieUtils.addRefreshTokenCookie(res, loginRes.getRefreshToken());

        Long academyId = null;
        if (user.getAcademies() != null && !user.getAcademies().isEmpty()) {
            academyId = user.getAcademies().get(0).getId();
        }

        return LoginResponse.builder()
                .accessToken(loginRes.getAccessToken())
                .academyId(academyId)
                .build();
    }

    private LoginResponse needPhoneAuthResponse(String socialId) {
        return LoginResponse.builder()
                .accessToken("NEED_PHONE_AUTH")
                .socialId(socialId)
                .build();
    }

    protected abstract Optional<UserEntity> findUserBySocialId(String socialId);
    protected abstract void saveNewSocialUser(String socialId);
}
