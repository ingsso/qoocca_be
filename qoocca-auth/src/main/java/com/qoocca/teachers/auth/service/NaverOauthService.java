package com.qoocca.teachers.auth.service;

import com.qoocca.teachers.common.global.utils.CookieUtils;
import com.qoocca.teachers.db.user.entity.OauthProvider;
import com.qoocca.teachers.db.user.entity.UserEntity;
import com.qoocca.teachers.common.auth.model.LoginResponse;
import com.qoocca.teachers.db.user.repository.UserRepository;
import com.qoocca.teachers.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
public class NaverOauthService extends SocialOauthService {

    @Value("${naver.client-id}") private String clientId;
    @Value("${naver.client-secret}") private String clientSecret;
    @Value("${naver.redirect-uri}") private String redirectUri;

    public NaverOauthService(JwtTokenProvider jwtTokenProvider, UserRepository userRepository,
                             RestTemplate restTemplate, CookieUtils cookieUtils) {
        super(jwtTokenProvider, userRepository, restTemplate, cookieUtils);
    }

    @Override
    public OauthProvider getProvider() {
        return OauthProvider.NAVER;
    }

    @Override
    public LoginResponse login(String code, HttpServletResponse response) {
        String naverAccessToken = getNaverAccessToken(code);

        Map<String, Object> userInfo = getNaverUserInfo(naverAccessToken);
        @SuppressWarnings("unchecked")
        Map<String, Object> naverResponse = (Map<String, Object>) userInfo.get("response");

        String naverId = String.valueOf(naverResponse.get("id"));

        return processSocialLogin(naverId, response);
    }

    @Override
    protected Optional<UserEntity> findUserBySocialId(String socialId) {
        return userRepository.findByNaverId(socialId);
    }

    @Override
    protected void saveNewSocialUser(String socialId) {
        userRepository.save(
                UserEntity
                        .builder()
                        .naverId(socialId)
                        .role("ROLE_USER")
                        .serviceAgree(false)
                        .privacyAgree(false)
                        .thirdPartyAgree(false)
                        .marketingAgree(false)
                        .alarm(true)
                        .build()
        );
    }

    private String getNaverAccessToken(String code) {
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        return (String) response.getBody().get("access_token");
    }

    private Map<String, Object> getNaverUserInfo(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                userInfoUrl,
                HttpMethod.GET,
                request,
                new ParameterizedTypeReference<>() {
                }
        );

        return response.getBody();
    }
}