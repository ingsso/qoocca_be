package com.example.qoocca_be.user.service;

import com.example.qoocca_be.global.exception.CustomException;
import com.example.qoocca_be.global.exception.ErrorCode;
import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.model.LoginResponseDto;
import com.example.qoocca_be.user.model.SocialLinkRequestDto;
import com.example.qoocca_be.user.model.UserRequestDto;
import com.example.qoocca_be.user.repository.UserRepository;
import com.example.qoocca_be.global.jwt.JwtTokenProvider;
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

    private void setAgreements(UserEntity user, UserRequestDto.AgreementsRequest agreements) {
        if (agreements == null) {
            return;
        }

        user.setServiceAgree(agreements.isService());
        user.setPrivacyAgree(agreements.isPrivacy());
        user.setThirdPartyAgree(agreements.isThirdParty());
        user.setMarketingAgree(agreements.isMarketing());
    }

    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
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
                    if (req.getAgreements() != null) {
                        setAgreements(existingUser, req.getAgreements());
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .userName(req.getUsername())
                            .email(req.getEmail())
                            .password(passwordEncoder.encode(req.getPassword()))
                            .phoneNumber(req.getPhone())
                            .role("ROLE_USER")
                            .alarm(true)
                            .build();
                    setAgreements(newUser, req.getAgreements());
                    return newUser;
                });

        userRepository.save(userEntity);
        smsService.deleteVerifiedState(req.getPhone());

        return jwtTokenProvider.generateTokens(userEntity.getId(), userEntity.getRole());
    }

    @Transactional
    public LoginResponseDto linkSocialAccount(SocialLinkRequestDto req) {
        String cleanPhone = req.phone().replaceAll("[^0-9]", "");
        smsService.checkIsVerified(cleanPhone);

        UserEntity tempSocialUser = ("kakao".equals(req.provider())
                ? userRepository.findByKakaoId(req.socialId())
                : userRepository.findByNaverId(req.socialId()))
                .orElseThrow(() -> new RuntimeException("소셜 계정을 찾을 수 없습니다."));

        Optional<UserEntity> existingUserOpt = userRepository.findByPhoneNumber(cleanPhone);

        if (existingUserOpt.isPresent()) {
            UserEntity existingUser = existingUserOpt.get();

            if (req.agreements() != null) {
                validateRequiredAgreements(req.agreements());
                setAgreements(existingUser, req.agreements());
            } else {
                existingUser.setServiceAgree(true);
                existingUser.setPrivacyAgree(true);
                existingUser.setThirdPartyAgree(true);
            }

            if ("kakao".equals(req.provider())) existingUser.setKakaoId(req.socialId());
            else if ("naver".equals(req.provider())) existingUser.setNaverId(req.socialId());

            // 기존 유저 정보 저장 강제
            userRepository.save(existingUser);

            if (!existingUser.getId().equals(tempSocialUser.getId())) {
                userRepository.delete(tempSocialUser);
                userRepository.flush();
            }

            smsService.deleteVerifiedState(cleanPhone);
            return jwtTokenProvider.generateTokens(existingUser.getId(), existingUser.getRole());
        }

        validateRequiredAgreements(req.agreements());

        tempSocialUser.setPhoneNumber(cleanPhone);
        setAgreements(tempSocialUser, req.agreements());
        userRepository.save(tempSocialUser);

        smsService.deleteVerifiedState(cleanPhone);
        return jwtTokenProvider.generateTokens(tempSocialUser.getId(), tempSocialUser.getRole());
    }

    private void validateRequiredAgreements(UserRequestDto.AgreementsRequest agreements) {
        if (agreements == null ||
                !agreements.isService() ||
                !agreements.isPrivacy() ||
                !agreements.isThirdParty()) {
            throw new RuntimeException("필수 약관 동의가 누락되었습니다.");
        }
    }
}
