package com.qoocca.be.user.controller;

import com.qoocca.be.global.utils.CookieUtils;
import com.qoocca.be.user.model.LoginRequestDto;
import com.qoocca.be.user.model.LoginResponseDto;
import com.qoocca.be.user.model.UserRequestDto;
import com.qoocca.be.user.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "User API", description = "회원 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final SmsService smsService;
    private final CookieUtils cookieUtils;

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody UserRequestDto req, HttpServletResponse res) {
        LoginResponseDto tokens = userService.signup(req);
        cookieUtils.addRefreshTokenCookie(res, tokens.getRefreshToken());
        return ResponseEntity.ok(new LoginResponseDto(tokens.getAccessToken(), null));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto req, HttpServletResponse res) {
        LoginResponseDto tokens = authService.login(req);
        cookieUtils.addRefreshTokenCookie(res, tokens.getRefreshToken());
        return ResponseEntity.ok(new LoginResponseDto(tokens.getAccessToken(), null));
    }

    @Operation(summary = "소셜 로그인")
    @PostMapping("/{provider}")
    public ResponseEntity<?> socialLogin(@PathVariable String provider,
                                         @RequestBody Map<String, String> body,
                                         HttpServletResponse res) {
        String code = body.get("code");
        return ResponseEntity.ok(authService.socialLogin(provider, code, res));
    }

    @Operation(summary = "Access Token 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest req) {
        String refreshToken = cookieUtils.getRefreshToken(req);;
        LoginResponseDto newTokens = authService.refreshAccessToken(refreshToken);
        return ResponseEntity.ok(newTokens);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken, HttpServletResponse res) {
        authService.logout(accessToken);
        cookieUtils.deleteRefreshTokenCookie(res);
        return ResponseEntity.ok("로그아웃 성공");
    }

    @Operation(summary = "계정 연동")
    @PostMapping("/link-social")
    public ResponseEntity<?> linkSocial(@RequestBody Map<String, String> body, HttpServletResponse res) {
        String phone = body.get("phone");
        String socialId = body.get("socialId");
        String provider = body.get("provider");
        Boolean agree = Boolean.valueOf(body.get("agree"));

        LoginResponseDto tokens = userService.linkSocialAccount(phone, socialId, provider, agree);
        cookieUtils.addRefreshTokenCookie(res, tokens.getRefreshToken());
        return ResponseEntity.ok(new LoginResponseDto(tokens.getAccessToken(), null));
    }

    @Operation(summary = "인증번호 전송")
    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        smsService.sendVerificationCode(phone);
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @Operation(summary = "인증번호 검증")
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");

        return ResponseEntity.ok(smsService.verifyCode(phone, code));
    }
}