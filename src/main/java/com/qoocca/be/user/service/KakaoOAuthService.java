package com.qoocca.be.user.service;

import com.qoocca.be.global.utils.CookieUtils;
import com.qoocca.be.user.entity.OauthProvider;
import com.qoocca.be.user.entity.UserEntity;
import com.qoocca.be.user.model.LoginResponseDto;
import com.qoocca.be.user.repository.UserRepository;
import com.qoocca.be.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
public class KakaoOAuthService extends SocialOauthService {

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public KakaoOAuthService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository,
                             RestTemplate restTemplate, CookieUtils cookieUtils) {
        super(jwtTokenProvider, userRepository, restTemplate, cookieUtils);
    }

    @Override
    public OauthProvider getProvider() {
        return OauthProvider.KAKAO;
    }

    @Override
    public LoginResponseDto login(String code, HttpServletResponse response) {
        String kakaoAccessToken = getKakaoAccessToken(code);

        Map<String, Object> userInfo = getKakaoUserInfo(kakaoAccessToken);
        String kakaoId = String.valueOf(userInfo.get("id"));

        return processSocialLogin(kakaoId, response);
    }

    @Override
    protected Optional<UserEntity> findUserBySocialId(String socialId) {
        return userRepository.findByKakaoId(socialId);
    }

    @Override
    protected void saveNewSocialUser(String socialId) {
        userRepository.save(UserEntity.builder()
                .kakaoId(socialId)
                .role("ROLE_USER")
                .build());
    }

    private String getKakaoAccessToken(String code) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        return (String) restTemplate.postForEntity(tokenUrl, request, Map.class).getBody().get("access_token");
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);
        return (Map<String, Object>) restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class).getBody();
    }
}