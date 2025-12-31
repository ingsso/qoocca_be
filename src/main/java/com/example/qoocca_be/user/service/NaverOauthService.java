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
public class NaverOauthService {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${naver.client-id}")
    private String clientId;

    @Value("${naver.client-secret}")
    private String clientSecret;

    @Value("${naver.redirect-uri}")
    private String redirectUri;

    public LoginResponseDto naverLogin(String code, HttpServletResponse response) {
        String naverAccessToken = getNaverAccessToken(code);
        Map<String, Object> userInfo = getNaverUserInfo(naverAccessToken);
        Map<String, Object> naverResponse = (Map<String, Object>) userInfo.get("response");

        if (naverResponse == null) {
            throw new RuntimeException("네이버 사용자 정보를 가져오는 데 실패했습니다.");
        }

        String naverId = String.valueOf(naverResponse.get("id"));

        return userRepository.findByNaverId(naverId)
                .map(user -> {
                    if (user.getPhoneNumber() == null) {
                        return LoginResponseDto.builder()
                                .accessToken("NEED_PHONE_AUTH")
                                .refreshToken(naverId)
                                .build();
                    }
                    return generateLoginResponse(user, response);
                })
                .orElseGet(() -> {
                    UserEntity newUserEntity = UserEntity.builder()
                            .naverId(naverId)
                            .role("ROLE_USER")
                            .build();
                    userRepository.save(newUserEntity);
                    return LoginResponseDto.builder()
                            .accessToken("NEED_PHONE_AUTH")
                            .refreshToken(naverId)
                            .build();
                });
    }

    private LoginResponseDto generateLoginResponse(UserEntity userEntity, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.generateAccessToken(userEntity.getNaverId(), userEntity.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(userEntity.getNaverId(), userEntity.getRole());
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
        ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);

        return (String) response.getBody().get("access_token");
    }

    private Map<String, Object> getNaverUserInfo(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<?> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(userInfoUrl, HttpMethod.GET, request, Map.class);

        return (Map<String, Object>) response.getBody();
    }
}