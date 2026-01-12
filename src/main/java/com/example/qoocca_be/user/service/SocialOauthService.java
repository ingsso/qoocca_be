package com.example.qoocca_be.user.service;

import com.example.qoocca_be.global.jwt.JwtTokenProvider;
import com.example.qoocca_be.global.utils.CookieUtils;
import com.example.qoocca_be.user.entity.OauthProvider;
import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.model.LoginResponseDto;
import com.example.qoocca_be.user.repository.UserRepository;
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
    public abstract LoginResponseDto login(String code, HttpServletResponse response);

    protected LoginResponseDto processSocialLogin(String socialId, HttpServletResponse response) {
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

    private LoginResponseDto generateLoginResponse(UserEntity user, HttpServletResponse res) {
        LoginResponseDto loginRes = jwtTokenProvider.generateTokens(user.getId(), user.getRole(), res);
        cookieUtils.addRefreshTokenCookie(res, loginRes.getRefreshToken());

        Long academyId = null;
        if (user.getAcademies() != null && !user.getAcademies().isEmpty()) {
            academyId = user.getAcademies().get(0).getId();
        }

        return LoginResponseDto.builder()
                .accessToken(loginRes.getAccessToken())
                .academyId(academyId)
                .build();
    }

    private LoginResponseDto needPhoneAuthResponse(String socialId) {
        return LoginResponseDto.builder()
                .accessToken("NEED_PHONE_AUTH")
                .socialId(socialId)
                .build();
    }

    protected abstract Optional<UserEntity> findUserBySocialId(String socialId);
    protected abstract void saveNewSocialUser(String socialId);
}
