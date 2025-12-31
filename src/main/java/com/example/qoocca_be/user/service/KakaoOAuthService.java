package com.example.qoocca_be.user.service;

import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.model.LoginResponseDto;
import com.example.qoocca_be.user.repository.UserRepository;
import com.example.qoocca_be.user.security.util.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOAuthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client-id}")
    private String clientId;

    @Value("${kakao.redirect-uri}")
    private String redirectUri;

    public LoginResponseDto kakaoLogin(String code, HttpServletResponse response) {
        String kakaoAccessToken = getKakaoAccessToken(code);
        Map<String, Object> userInfo = getKakaoUserInfo(kakaoAccessToken);
        String kakaoId = String.valueOf(userInfo.get("id"));

        return userRepository.findByKakaoId(kakaoId)
                .map(user -> {
                    if (user.getPhoneNumber() == null) {
                        return LoginResponseDto.builder()
                                .accessToken("NEED_PHONE_AUTH")
                                .refreshToken(kakaoId)
                                .build();
                    }
                    return generateLoginResponse(user, response);
                })
                .orElseGet(() -> {
                    UserEntity newUserEntity = UserEntity.builder()
                            .kakaoId(kakaoId)
                            .role("ROLE_USER")
                            .build();
                    userRepository.save(newUserEntity);
                    return LoginResponseDto.builder()
                            .accessToken("NEED_PHONE_AUTH")
                            .refreshToken(kakaoId)
                            .build();
                });
    }

    private LoginResponseDto generateLoginResponse(UserEntity userEntity, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.generateAccessToken(userEntity.getKakaoId(), userEntity.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userEntity.getKakaoId(), userEntity.getRole());
        setRefreshTokenCookie(response, refreshToken);
        return new LoginResponseDto(accessToken, null);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        refreshCookie.setPath("/");
        response.addCookie(refreshCookie);
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
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");

    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

        return (Map<String, Object>) response.getBody();
    }
}