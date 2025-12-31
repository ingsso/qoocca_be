package com.example.qoocca_be.user.controller;

import com.example.qoocca_be.user.entity.UserEntity;
import com.example.qoocca_be.user.model.LoginRequestDto;
import com.example.qoocca_be.user.model.LoginResponseDto;
import com.example.qoocca_be.user.model.RedisDao;
import com.example.qoocca_be.user.model.UserRequestDto;
import com.example.qoocca_be.user.repository.UserRepository;
import com.example.qoocca_be.user.security.util.JwtTokenProvider;
import com.example.qoocca_be.user.service.KakaoOAuthService;
import com.example.qoocca_be.user.service.NaverOauthService;
import com.example.qoocca_be.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final KakaoOAuthService kakaoOAuthService;
    private final NaverOauthService naverOauthService;
    private final UserRepository userRepository;
    private final RedisDao redisDao;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRequestDto req,
                                                   HttpServletResponse res) {
        try {
            LoginResponseDto tokens = userService.signup(req);
            return getRefreshCookie(res, tokens);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto req,
                                                  HttpServletResponse res) {
        LoginResponseDto tokens = userService.login(req);

        return getRefreshCookie(res, tokens);
    }

    @PostMapping("/kakao")
    public ResponseEntity<?> kakaoLogin(@RequestBody Map<String, String> body,
                                        HttpServletResponse res) {
        String code = body.get("code");
        return ResponseEntity.ok(kakaoOAuthService.kakaoLogin(code, res));
    }

    @PostMapping("/naver")
    public ResponseEntity<?> naverLogin(@RequestBody Map<String, String> body,
                                        HttpServletResponse res) {
        String code = body.get("code");
        return ResponseEntity.ok(naverOauthService.naverLogin(code, res));
    }

    @PostMapping("/link-social")
    @Transactional
    public ResponseEntity<?> linkSocial(@RequestBody Map<String, String> body, HttpServletResponse response) {
        String phone = body.get("phone");
        String socialId = body.get("socialId");
        String provider = body.get("provider");

        String verified = (String) redisDao.getValues("SMS_VERIFIED:" + phone);
        if (verified == null) return ResponseEntity.badRequest().body("인증이 필요합니다.");

        try {
            Optional<UserEntity> existingUserOpt = userRepository.findByPhoneNumber(phone);

            if (existingUserOpt.isPresent()) {
                UserEntity existingUserEntity = existingUserOpt.get();

                if ("k".equals(provider)) {
                    userRepository.findByKakaoId(socialId).ifPresent(user -> {
                        userRepository.delete(user);
                        userRepository.flush();
                    });
                    existingUserEntity.setKakaoId(socialId);
                } else if ("n".equals(provider)) {
                    userRepository.findByNaverId(socialId).ifPresent(user -> {
                        userRepository.delete(user);
                        userRepository.flush();
                    });
                    existingUserEntity.setNaverId(socialId);
                }

                userRepository.save(existingUserEntity);

                return generateLoginResponse(existingUserEntity, response);
            } else {
                UserEntity socialUserEntity = null;

                if ("k".equals(provider)) {
                    socialUserEntity = userRepository.findByKakaoId(socialId).orElse(null);
                } else if ("n".equals(provider)) {
                    socialUserEntity = userRepository.findByNaverId(socialId).orElse(null);
                }

                if (socialUserEntity == null) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body("해당 소셜 유저를 찾을 수 없습니다.");
                }

                socialUserEntity.setPhoneNumber(phone);
                userRepository.save(socialUserEntity);

                return generateLoginResponse(socialUserEntity, response);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("연결 실패: " + e.getMessage());
        }
    }

    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");

        Optional<UserEntity> existingUser = userRepository.findByPhoneNumber(phone);

        if (existingUser.isPresent() && existingUser.get().getPassword() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 가입된 휴대폰 번호입니다.");
        }

        String verificationCode = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 1000000));
        redisDao.setValues("SMS:" + phone, verificationCode, Duration.ofMinutes(3));

        System.out.println("휴대폰: " + phone + " / 인증번호: " + verificationCode);
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");

        String savedCode = (String) redisDao.getValues("SMS:" + phone);

        if (savedCode != null && savedCode.equals(code)) {
            redisDao.deleteValues("SMS:" + phone);

            redisDao.setValues("SMS_VERIFIED:" + phone, "true", Duration.ofMinutes(5));

            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.badRequest().body("인증번호가 일치하지 않습니다.");
        }
    }

    private ResponseEntity<?> generateLoginResponse(UserEntity userEntity, HttpServletResponse response) {
        String identifier = (userEntity.getEmail() != null) ? userEntity.getEmail() : userEntity.getKakaoId();

        String accessToken = jwtTokenProvider.generateAccessToken(identifier, userEntity.getRole());
        String refreshToken = jwtTokenProvider.generateRefreshToken(identifier, userEntity.getRole());

        LoginResponseDto tokens = new LoginResponseDto(accessToken, refreshToken);
        return getRefreshCookie(response, tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest req) {
        String refreshToken = null;
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if (cookie.getName().equals("refreshToken")) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("유효하지 않는 refreshToken 입니다.");
        }

        if (jwtTokenProvider.isBlacklisted(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("로그아웃된 토큰입니다.");
        }

        String identifier = jwtTokenProvider.getIdentifierFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);

        if (!jwtTokenProvider.validateRefreshToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("서버의 토큰 정보와 일치하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(identifier, role);

        return ResponseEntity.ok(new LoginResponseDto(newAccessToken, null));
    }

    @PostMapping("/logout")
    public String logout(@RequestHeader("Authorization") String accessToken,
                         HttpServletResponse res) {
        String token = accessToken.replace("Bearer ", "");
        String identifier = jwtTokenProvider.getIdentifierFromToken(token);

        jwtTokenProvider.deleteRefreshToken(identifier);
        jwtTokenProvider.addToBlacklist(token);

        Cookie refreshCookie = new Cookie("refreshToken", null);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setMaxAge(0);
        refreshCookie.setPath("/");
        res.addCookie(refreshCookie);

        return "로그아웃 성공";
    }

    private ResponseEntity<LoginResponseDto> getRefreshCookie(HttpServletResponse res,
                                                              LoginResponseDto tokens) {
        Cookie refreshCookie = new Cookie("refreshToken", tokens.getRefreshToken());
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        res.addCookie(refreshCookie);

        return ResponseEntity.ok(new LoginResponseDto(tokens.getAccessToken(), null));
    }
}
