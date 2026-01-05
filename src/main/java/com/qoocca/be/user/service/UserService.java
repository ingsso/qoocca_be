package com.qoocca.be.user.service;

import com.qoocca.be.global.exception.CustomException;
import com.qoocca.be.global.exception.ErrorCode;
import com.qoocca.be.user.entity.UserEntity;
import com.qoocca.be.user.model.LoginResponseDto;
import com.qoocca.be.user.model.UserRequestDto;
import com.qoocca.be.user.repository.UserRepository;
import com.qoocca.be.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SmsService smsService;

    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    public LoginResponseDto signup(UserRequestDto req) {
        smsService.checkIsVerified(req.getPhone());

        UserEntity userEntity = userRepository.findByPhoneNumber(req.getPhone())
                .map(existingUser -> {
                    if (existingUser.getPassword() != null) {
                        throw new RuntimeException("이미 사용 중인 전화번호 입니다.");
                    }
                    existingUser.setEmail(req.getEmail());
                    existingUser.setUserName(req.getUsername());
                    existingUser.setPassword(passwordEncoder.encode(req.getPassword()));
                    if (existingUser.getAgree() == null || !existingUser.getAgree()) {
                        existingUser.setAgree(req.getAgree());
                    }
                    return existingUser;
                })
                .orElseGet(() -> UserEntity.builder()
                                .userName(req.getUsername())
                                .email(req.getEmail())
                                .password(passwordEncoder.encode(req.getPassword()))
                                .phoneNumber(req.getPhone())
                                .agree(req.getAgree())
                                .alarm(true)
                                .role("ROLE_USER")
                                .build()
                );

        userRepository.save(userEntity);
        smsService.deleteVerifiedState(req.getPhone());

        return jwtTokenProvider.generateTokens(userEntity.getId(), userEntity.getRole());
    }

    @Transactional
    public LoginResponseDto linkSocialAccount(String phone, String socialId, String provider, Boolean agree) {
        String cleanPhone = phone.replaceAll("[^0-9]", "");
        smsService.checkIsVerified(cleanPhone);

        UserEntity tempSocialUser = ("kakao".equals(provider)
                ? userRepository.findByKakaoId(socialId)
                : userRepository.findByNaverId(socialId))
                .orElseThrow(() -> new RuntimeException("소셜 계정을 찾을 수 없습니다."));

        Optional<UserEntity> existingUserOpt = userRepository.findByPhoneNumber(cleanPhone);

        if (existingUserOpt.isPresent()) {
            UserEntity existingUser = existingUserOpt.get();

            if (!existingUser.getAgree()) {
                if (agree == null || !agree) {
                    throw new RuntimeException("약관 동의가 필요합니다.");
                }
                existingUser.setAgree(true);
            }

            if (!existingUser.getId().equals(tempSocialUser.getId())) {
                userRepository.delete(tempSocialUser);
                userRepository.flush();
            }

            if ("kakao".equals(provider)) existingUser.setKakaoId(socialId);
            else if ("naver".equals(provider)) existingUser.setNaverId(socialId);

            userRepository.save(existingUser);
            smsService.deleteVerifiedState(cleanPhone);
            return jwtTokenProvider.generateTokens(existingUser.getId(), existingUser.getRole());
        }

        if (agree == null || !agree) {
            throw new RuntimeException("약관 동의가 필요합니다.");
        }

        tempSocialUser.setPhoneNumber(cleanPhone);
        userRepository.save(tempSocialUser);

        smsService.deleteVerifiedState(cleanPhone);
        return jwtTokenProvider.generateTokens(tempSocialUser.getId(), tempSocialUser.getRole());
    }
}
