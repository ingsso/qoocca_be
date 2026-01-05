package com.example.qoocca_be.user.service;

import com.example.qoocca_be.global.exception.CustomException;
import com.example.qoocca_be.global.exception.ErrorCode;
import com.example.qoocca_be.global.jwt.JwtTokenProvider;
import com.example.qoocca_be.user.entity.OauthProvider;
import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.model.LoginRequestDto;
import com.example.qoocca_be.user.model.LoginResponseDto;
import com.example.qoocca_be.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final List<SocialOauthService> socialOauthServices;

    public LoginResponseDto socialLogin(String providerStr, String code, HttpServletResponse res) {
        OauthProvider provider = OauthProvider.fromString(providerStr);

        SocialOauthService service = socialOauthServices.stream()
                .filter(s -> s.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SOCIAL_PROVIDER_NOT_FOUND));

        return service.login(code, res);
    }

    public LoginResponseDto login(LoginRequestDto req) {
        UserEntity userEntity = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getPassword(), userEntity.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        return jwtTokenProvider.generateTokens(userEntity.getId(), userEntity.getRole());
    }

    @Transactional
    public void logout(String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        jwtTokenProvider.deleteRefreshToken(userId);
        jwtTokenProvider.addToBlacklist(token);
    }

    @Transactional(readOnly = true)
    public LoginResponseDto refreshAccessToken(String refreshToken) {
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        if (jwtTokenProvider.isBlacklisted(refreshToken)) {
            throw new CustomException(ErrorCode.LOGOUT_TOKEN);
        }

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            throw new CustomException(ErrorCode.TOKEN_MISMATCH);
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);
        String newAccessToken = jwtTokenProvider.generateAccessToken(userId, role);

        return new LoginResponseDto(newAccessToken, null);
    }
}
