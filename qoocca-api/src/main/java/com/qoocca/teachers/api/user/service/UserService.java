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

    // [추가] 다른 서비스에서 유저 정보를 조회할 때 꼭 필요한 메서드입니다.
    public UserEntity findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    // [최적화] 비밀번호 암호화 연산은 CPU를 많이 써서 오래 걸리므로 트랜잭션 밖에서 미리 실행
    public LoginResponse signup(UserRequest req, HttpServletResponse res) {
        smsService.checkIsVerified(req.getPhone());

        // 트랜잭션 시작 전 무거운 암호화 작업 수행 (커넥션 점유 시간 단축)
        String encodedPassword = passwordEncoder.encode(req.getPassword());

        UserEntity userEntity = saveUser(req, encodedPassword);

        smsService.deleteVerifiedState(req.getPhone());

        // 학원 정보를 한 번에 가져오는 최적화된 응답 빌드 호출
        return buildLoginResponse(userEntity, res);
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

        return buildLoginResponse(tempSocialUser, res);
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