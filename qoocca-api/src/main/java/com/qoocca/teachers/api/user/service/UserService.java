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

import java.util.List;

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

    public LoginResponse linkSocialAccount(SocialLinkRequest req, HttpServletResponse res) {
        String cleanPhone = normalizePhone(req.phone());
        smsService.checkIsVerified(cleanPhone);
        UserEntity userEntity = userWriteService.linkSocialAccount(req, cleanPhone);
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

    private LoginResponse buildLoginResponse(UserEntity user, HttpServletResponse res) {

        LoginResponse tokenResponse =
                jwtTokenProvider.generateTokens(user.getId(), user.getRole(), res);

        List<AcademyEntity> academies =
                academyRepository.findAllByUserId(user.getId());

        List<LoginResponse.AcademyListResponse> academyInfos = academies.stream()
                .map(a -> LoginResponse.AcademyListResponse.builder()
                        .academyId(a.getId())
                        .name(a.getName())
                        .approvalStatus(a.getApprovalStatus().name())
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
