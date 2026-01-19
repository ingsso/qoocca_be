package com.example.qoocca_be.user.service;

import com.example.qoocca_be.global.exception.CustomException;
import com.example.qoocca_be.global.exception.ErrorCode;
import com.example.qoocca_be.global.jwt.JwtTokenProvider;
import com.example.qoocca_be.user.entity.OauthProvider;
import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.model.LoginRequest;
import com.example.qoocca_be.user.model.LoginResponse;
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

    public LoginResponse socialLogin(String providerStr, String code, HttpServletResponse res) {
        OauthProvider provider = OauthProvider.fromString(providerStr);

        SocialOauthService service = socialOauthServices.stream()
                .filter(s -> s.getProvider() == provider)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.SOCIAL_PROVIDER_NOT_FOUND));

        return service.login(code, res);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req, HttpServletResponse res) {
        UserEntity userEntity = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(req.getPassword(), userEntity.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        LoginResponse tokens = jwtTokenProvider.generateTokens(userEntity.getId(), userEntity.getRole(), res);

        return LoginResponse.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(null)
                .academyId(userEntity.getAcademies().isEmpty() ? null : userEntity.getAcademies().get(0).getId())
                .build();
    }

    @Transactional
    public void logout(String accessToken) {
        String token = accessToken.replace("Bearer ", "");
        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        jwtTokenProvider.deleteRefreshToken(userId);
        jwtTokenProvider.addToBlacklist(token);
    }

    @Transactional(readOnly = true)
    public LoginResponse refreshAccessToken(String refreshToken) {
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

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .academyId(user.getAcademies().isEmpty() ? null : user.getAcademies().get(0).getId())
                .build();
    }
}
