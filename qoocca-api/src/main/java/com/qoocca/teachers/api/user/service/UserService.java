package com.qoocca.teachers.api.user.service;

import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.user.entity.UserEntity;
import com.qoocca.teachers.common.auth.model.LoginResponse;
import com.qoocca.teachers.common.auth.model.SocialLinkRequest;
import com.qoocca.teachers.common.auth.model.UserRequest;
import com.qoocca.teachers.db.user.repository.UserRepository;
import com.qoocca.teachers.auth.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserWriteService userWriteService;
    private final AcademyRepository academyRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SmsService smsService;


    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }


    public LoginResponse signup(UserRequest req, HttpServletResponse res) {
        String cleanPhone = normalizePhone(req.getPhone());
        smsService.checkIsVerified(cleanPhone);
        UserEntity userEntity = userWriteService.signup(req, cleanPhone);
        smsService.deleteVerifiedState(cleanPhone);

        LoginResponse response = jwtTokenProvider.generateTokens(userEntity.getId(), userEntity.getRole(), res);
        addAcademyIdToResponse(response, userEntity.getId());
        return response;
    }
   

    @Transactional
    protected UserEntity saveUser(UserRequest req, String encodedPassword) {
        UserEntity userEntity = userRepository.findByPhoneNumber(req.getPhone())
                .map(existingUser -> {
                    if (existingUser.getPassword() != null) {
                        throw new CustomException(ErrorCode.PHONE_ALREADY_IN_USE);
                    }
                    existingUser.setEmail(req.getEmail());
                    existingUser.setUserName(req.getUsername());
                    existingUser.setPassword(encodedPassword);
                    if (req.getAgreements() != null) {
                        setAgreements(existingUser, req.getAgreements());
                    }
                    return existingUser;
                })
                .orElseGet(() -> {
                    UserEntity newUser = UserEntity.builder()
                            .userName(req.getUsername())
                            .email(req.getEmail())
                            .password(encodedPassword)
                            .phoneNumber(req.getPhone())
                            .role("ROLE_USER")
                            .alarm(true)
                            .build();
                    setAgreements(newUser, req.getAgreements());
                    return newUser;
                });

        return userRepository.save(userEntity);
    }

    public LoginResponse linkSocialAccount(SocialLinkRequest req, HttpServletResponse res) {
        String cleanPhone = normalizePhone(req.phone());
        smsService.checkIsVerified(cleanPhone);

        UserEntity userEntity = userWriteService.linkSocialAccount(req, cleanPhone);


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
        return buildLoginResponse(userEntity, res);
    }


    private String normalizePhone(String phone) {
        if (phone == null) {
            return "";
        }
        return phone.replaceAll("[^0-9]", "");
    }

    private void addAcademyIdToResponse(LoginResponse response, Long userId) {
        academyRepository.findAllByUserId(userId).stream()
                .findFirst()
                .ifPresent(academy -> response.setAcademyId(academy.getId()));
    }

    private void setAgreements(UserEntity user, UserRequest.AgreementsRequest agreements) {
        if (agreements == null) return;
        user.setServiceAgree(agreements.isService());
        user.setPrivacyAgree(agreements.isPrivacy());
        user.setThirdPartyAgree(agreements.isThirdParty());
        user.setMarketingAgree(agreements.isMarketing());
    }

    // [최적화] 학원 조회를 한 번으로 통일하고 응답 객체를 완성함

    private LoginResponse buildLoginResponse(UserEntity user, HttpServletResponse res) {
        LoginResponse tokenResponse = jwtTokenProvider.generateTokens(user.getId(), user.getRole(), res);

        // Academy 테이블 인덱스를 활용한 단일 쿼리
        List<AcademyEntity> academies = academyRepository.findAllByUserId(user.getId());

        List<LoginResponse.AcademyListResponse> academyInfos = academies.stream()
                .map(a -> LoginResponse.AcademyListResponse.builder()
                        .academyId(a.getId())
                        .name(a.getName())
                        .approvalStatus(a.getApprovalStatus().name())
                        .build())
                .toList();

        tokenResponse.setAcademies(academyInfos);

        if (academyInfos.size() == 1) {
            tokenResponse.setAcademyId(academyInfos.get(0).getAcademyId());
        } else {
            tokenResponse.setAcademyId(null);
        }

        return tokenResponse;
    }

    private void validateRequiredAgreements(UserRequest.AgreementsRequest agreements) {
        if (agreements == null || !agreements.isService() || !agreements.isPrivacy() || !agreements.isThirdParty()) {
            throw new CustomException(ErrorCode.REQUIRED_AGREEMENTS_MISSING);
        }
    }
}