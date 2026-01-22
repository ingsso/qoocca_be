package com.qoocca.teachers.user.service;

import com.qoocca.teachers.academy.entity.AcademyEntity;
import com.qoocca.teachers.academy.model.response.AcademyListResponse;
import com.qoocca.teachers.academy.repository.AcademyRepository;
import com.qoocca.teachers.global.exception.CustomException;
import com.qoocca.teachers.global.exception.ErrorCode;
import com.qoocca.teachers.user.entity.UserEntity;
import com.qoocca.teachers.user.model.LoginResponse;
import com.qoocca.teachers.user.model.SocialLinkRequest;
import com.qoocca.teachers.user.model.UserRequest;
import com.qoocca.teachers.user.repository.UserRepository;
import com.qoocca.teachers.global.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final AcademyRepository academyRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final SmsService smsService;

    private void setAgreements(UserEntity user, UserRequest.AgreementsRequest agreements) {
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
    public LoginResponse signup(UserRequest req, HttpServletResponse res) {
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

        LoginResponse response = jwtTokenProvider.generateTokens(userEntity.getId(), userEntity.getRole(), res);

        addAcademyIdToResponse(response, userEntity.getId());
        return response;
    }

    @Transactional
    public LoginResponse linkSocialAccount(SocialLinkRequest req, HttpServletResponse res) {
        String cleanPhone = req.phone().replaceAll("[^0-9]", "");
        smsService.checkIsVerified(cleanPhone);

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

            if ("kakao".equals(req.provider())) existingUser.setKakaoId(req.socialId());
            else if ("naver".equals(req.provider())) existingUser.setNaverId(req.socialId());

            userRepository.save(existingUser);

            if (!existingUser.getId().equals(tempSocialUser.getId())) {
                userRepository.delete(tempSocialUser);
                userRepository.flush();
            }

            smsService.deleteVerifiedState(cleanPhone);
            return buildLoginResponse(existingUser, res);

        }

        validateRequiredAgreements(req.agreements());

        tempSocialUser.setPhoneNumber(cleanPhone);
        setAgreements(tempSocialUser, req.agreements());
        userRepository.save(tempSocialUser);
        smsService.deleteVerifiedState(cleanPhone);

        LoginResponse response = jwtTokenProvider.generateTokens(tempSocialUser.getId(), tempSocialUser.getRole(), res);

        addAcademyIdToResponse(response, tempSocialUser.getId());
        return response;
    }

    private void addAcademyIdToResponse(LoginResponse response, Long userId) {
        academyRepository.findAllByUserId(userId).stream()
                .findFirst()
                .ifPresent(academy -> {
                    response.setAcademyId(academy.getId());
                });
    }

    private void validateRequiredAgreements(UserRequest.AgreementsRequest agreements) {
        if (agreements == null ||
                !agreements.isService() ||
                !agreements.isPrivacy() ||
                !agreements.isThirdParty()) {
            throw new RuntimeException("필수 약관 동의가 누락되었습니다.");
        }
    }

    private LoginResponse buildLoginResponse(UserEntity user, HttpServletResponse res) {

        LoginResponse tokenResponse =
                jwtTokenProvider.generateTokens(user.getId(), user.getRole(), res);

        List<AcademyEntity> academies =
                academyRepository.findAllByUserId(user.getId());

        List<AcademyListResponse> academyInfos = academies.stream()
                .map(a -> AcademyListResponse.builder()
                        .academyId(a.getId())
                        .name(a.getName())
                        .approvalStatus(a.getApprovalStatus())
                        .build())
                .toList();

        Long academyId = academyInfos.size() == 1
                ? academyInfos.get(0).getAcademyId()
                : null;

        tokenResponse.setAcademies(academyInfos);
        tokenResponse.setAcademyId(academyId);

        return tokenResponse;
    }

}
