package com.qoocca.teachers.api.user.service;

import com.qoocca.teachers.common.auth.model.SocialLinkRequest;
import com.qoocca.teachers.common.auth.model.UserRequest;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.user.entity.UserEntity;
import com.qoocca.teachers.db.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserWriteService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Retryable(
            retryFor = {CannotAcquireLockException.class, PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    public UserEntity signup(UserRequest req, String cleanPhone) {
        UserEntity userEntity = userRepository.findByPhoneNumber(cleanPhone)
                .map(existingUser -> {
                    if (existingUser.getPassword() != null) {
                        throw new CustomException(ErrorCode.PHONE_ALREADY_IN_USE);
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
                            .phoneNumber(cleanPhone)
                            .role("ROLE_USER")
                            .alarm(true)
                            .build();
                    setAgreements(newUser, req.getAgreements());
                    return newUser;
                });

        try {
            return userRepository.saveAndFlush(userEntity);
        } catch (DataIntegrityViolationException e) {
            throw mapIntegrityViolation(e);
        }
    }

    @Transactional
    @Retryable(
            retryFor = {CannotAcquireLockException.class, PessimisticLockingFailureException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2.0)
    )
    public UserEntity linkSocialAccount(SocialLinkRequest req, String cleanPhone) {
        UserEntity tempSocialUser = ("kakao".equals(req.provider())
                ? userRepository.findByKakaoId(req.socialId())
                : userRepository.findByNaverId(req.socialId()))
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Optional<UserEntity> existingUserOpt = userRepository.findByPhoneNumber(cleanPhone);

        if (existingUserOpt.isPresent()) {
            UserEntity existingUser = existingUserOpt.get();

            if (req.agreements() != null) {
                setAgreements(existingUser, req.agreements());
            } else {
                existingUser.setServiceAgree(true);
                existingUser.setPrivacyAgree(true);
                existingUser.setThirdPartyAgree(true);
            }

            if ("kakao".equals(req.provider())) {
                existingUser.setKakaoId(req.socialId());
            } else if ("naver".equals(req.provider())) {
                existingUser.setNaverId(req.socialId());
            }

            try {
                userRepository.saveAndFlush(existingUser);
            } catch (DataIntegrityViolationException e) {
                throw mapIntegrityViolation(e);
            }

            if (!existingUser.getId().equals(tempSocialUser.getId())) {
                userRepository.delete(tempSocialUser);
                userRepository.flush();
            }

            return existingUser;
        }

        validateRequiredAgreements(req.agreements());
        tempSocialUser.setPhoneNumber(cleanPhone);
        setAgreements(tempSocialUser, req.agreements());

        try {
            return userRepository.saveAndFlush(tempSocialUser);
        } catch (DataIntegrityViolationException e) {
            throw mapIntegrityViolation(e);
        }
    }

    private void setAgreements(UserEntity user, UserRequest.AgreementsRequest agreements) {
        if (agreements == null) {
            return;
        }
        user.setServiceAgree(agreements.isService());
        user.setPrivacyAgree(agreements.isPrivacy());
        user.setThirdPartyAgree(agreements.isThirdParty());
        user.setMarketingAgree(agreements.isMarketing());
    }

    private void validateRequiredAgreements(UserRequest.AgreementsRequest agreements) {
        if (agreements == null ||
                !agreements.isService() ||
                !agreements.isPrivacy() ||
                !agreements.isThirdParty()) {
            throw new CustomException(ErrorCode.REQUIRED_AGREEMENTS_MISSING);
        }
    }

    private boolean isPhoneUniqueViolation(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause() != null
                ? e.getMostSpecificCause().getMessage()
                : e.getMessage();
        if (message == null) {
            return false;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        return lower.contains("uk_user_phone_number") || lower.contains("user_phone_number");
    }

    private CustomException mapIntegrityViolation(DataIntegrityViolationException e) {
        if (isPhoneUniqueViolation(e)) {
            return new CustomException(ErrorCode.PHONE_ALREADY_IN_USE);
        }
        return new CustomException(ErrorCode.INVALID_INPUT_VALUE);
    }
}
