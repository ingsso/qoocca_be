package com.example.qoocca_be.user.service;

import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.model.LoginRequestDto;
import com.example.qoocca_be.user.model.LoginResponseDto;
import com.example.qoocca_be.user.model.RedisDao;
import com.example.qoocca_be.user.model.UserRequestDto;
import com.example.qoocca_be.user.repository.UserRepository;
import com.example.qoocca_be.user.security.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisDao redisDao;

    public LoginResponseDto signup(UserRequestDto req) {
        String verified = (String) redisDao.getValues("SMS_VERIFIED:" + req.getPhone());
        if (verified == null || !verified.equals("true")) {
            throw new RuntimeException("휴대폰 인증이 완료되지 않았습니다.");
        }

        UserEntity userEntity = userRepository.findByPhoneNumber(req.getPhone())
                .map(existingUser -> {
                    if (existingUser.getPassword() != null) {
                        throw new RuntimeException("이미 사용 중인 전화번호 입니다.");
                    }
                    existingUser.setEmail(req.getEmail());
                    existingUser.setUserName(req.getUsername());
                    existingUser.setPassword(passwordEncoder.encode(req.getPassword()));
                    return existingUser;
                })
                .orElseGet(() -> UserEntity.builder()
                                .userName(req.getUsername())
                                .email(req.getEmail())
                                .password(passwordEncoder.encode(req.getPassword()))
                                .phoneNumber(req.getPhone())
                                .role("ROLE_USER")
                                .agree(true)      // ✅ 추가
                                .alarm(true)      // ✅ 추가
                                .build()
                );

        userRepository.save(userEntity);
        redisDao.deleteValues("SMS_VERIFIED:" + req.getPhone());

        return generateTokens(userEntity);
    }

    public LoginResponseDto login(LoginRequestDto req) {
        UserEntity userEntity = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(req.getPassword(), userEntity.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return generateTokens(userEntity);
    }

    private LoginResponseDto generateTokens(UserEntity userEntity) {
        String identifier = userEntity.getEmail();
        if (identifier == null) {
            identifier = (userEntity.getKakaoId() != null) ? userEntity.getKakaoId() : userEntity.getNaverId();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(identifier, userEntity.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(identifier, userEntity.getRole());

        return new LoginResponseDto(accessToken, refreshToken);
    }
}
