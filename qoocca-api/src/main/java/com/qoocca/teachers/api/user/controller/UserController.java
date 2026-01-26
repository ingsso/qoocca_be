package com.qoocca.teachers.api.user.controller;

import com.qoocca.teachers.common.global.utils.CookieUtils;
import com.qoocca.teachers.common.auth.model.LoginRequest;
import com.qoocca.teachers.common.auth.model.LoginResponse;
import com.qoocca.teachers.common.auth.model.SocialLinkRequest;
import com.qoocca.teachers.common.auth.model.UserRequest;
import com.qoocca.teachers.auth.service.AuthService;
import com.qoocca.teachers.api.user.service.SmsService;
import com.qoocca.teachers.api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "User API", description = "회원가입, 로그인, SMS 인증 등 인증 관련 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final AuthService authService;
    private final SmsService smsService;
    private final CookieUtils cookieUtils;

    @Operation(summary = "회원가입", description = "일반 이메일 회원가입을 진행합니다. SMS 인증이 선행되어야 합니다.")
    @PostMapping("/signup")
    public ResponseEntity<LoginResponse> signup(@Valid @RequestBody UserRequest req, HttpServletResponse res) {
        LoginResponse tokens = userService.signup(req, res);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "계정 연동", description = "소셜 계정 정보를 기존 일반 계정에 연동합니다.")
    @PostMapping("/link-social")
    public ResponseEntity<LoginResponse> linkSocial(@RequestBody SocialLinkRequest dto, HttpServletResponse res) {
        LoginResponse tokens = userService.linkSocialAccount(dto, res);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "일반 로그인", description = "이메일과 비밀번호로 로그인을 진행합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req, HttpServletResponse res) {
        LoginResponse tokens = authService.login(req, res);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "소셜 로그인", description = "카카오 또는 네이버 인증 코드를 통해 로그인을 진행합니다.")
    @PostMapping("/{provider}")
    public ResponseEntity<LoginResponse> socialLogin(
            @Parameter(description = "소셜 제공자", example = "kakao") @PathVariable String provider,
            @RequestBody Map<String, String> body,
            HttpServletResponse res) {
        String code = body.get("code");
        LoginResponse tokens = authService.socialLogin(provider, code, res);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Access Token 재발급", description = "쿠키에 저장된 Refresh Token을 사용하여 새로운 Access Token을 발급합니다.")
    @ApiResponse(responseCode = "200", description = "재발급 성공")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(HttpServletRequest req, HttpServletResponse res) {
        String refreshToken = cookieUtils.getRefreshToken(req);
        LoginResponse newTokens = authService.refreshAccessToken(refreshToken);

        ResponseCookie accessTokenCookie = ResponseCookie.from("accessToken", newTokens.getAccessToken())
                .path("/")
                .maxAge(1800)
                .httpOnly(false)
                .secure(false)
                .sameSite("Lax")
                .build();

        res.addHeader(HttpHeaders.SET_COOKIE, accessTokenCookie.toString());
        return ResponseEntity.ok(newTokens);
    }

    @Operation(summary = "로그아웃", description = "토큰을 만료시키고 브라우저의 Refresh Token 쿠키를 삭제합니다.")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @Parameter(hidden = true) @RequestHeader("Authorization") String accessToken,
            HttpServletResponse res) {
        authService.logout(accessToken);
        cookieUtils.deleteRefreshTokenCookie(res);
        return ResponseEntity.ok("로그아웃 성공");
    }

    @Operation(summary = "SMS 인증번호 전송", description = "입력한 전화번호로 6자리 인증번호를 발송합니다.")
    @PostMapping("/send-code")
    public ResponseEntity<String> sendVerificationCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        smsService.sendVerificationCode(phone);
        return ResponseEntity.ok("인증번호가 발송되었습니다.");
    }

    @Operation(summary = "SMS 인증번호 검증", description = "발송된 인증번호와 사용자가 입력한 번호를 비교 검증합니다.")
    @PostMapping("/verify-code")
    public ResponseEntity<?> verifyCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");
        return ResponseEntity.ok(smsService.verifyCode(phone, code));
    }
}