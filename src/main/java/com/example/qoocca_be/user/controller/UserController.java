package com.example.qoocca_be.user.controller;

import com.example.qoocca_be.global.utils.CookieUtils;
import com.example.qoocca_be.user.model.LoginRequestDto;
import com.example.qoocca_be.user.model.LoginResponseDto;
import com.example.qoocca_be.user.model.SocialLinkRequestDto;
import com.example.qoocca_be.user.model.UserRequestDto;
import com.example.qoocca_be.user.service.AuthService;
import com.example.qoocca_be.user.service.SmsService;
import com.example.qoocca_be.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
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
    public ResponseEntity<?> signup(@Valid @RequestBody UserRequestDto req, HttpServletResponse res) {
        LoginResponseDto tokens = userService.signup(req, res);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "계정 연동")
    @PostMapping("/link-social")
    public ResponseEntity<?> linkSocial(@RequestBody SocialLinkRequestDto dto, HttpServletResponse res) {
        LoginResponseDto tokens = userService.linkSocialAccount(dto, res);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto req, HttpServletResponse res) {
        LoginResponseDto tokens = authService.login(req, res);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "소셜 로그인")
    @PostMapping("/{provider}")
    public ResponseEntity<?> socialLogin(@PathVariable String provider,
                                         @RequestBody Map<String, String> body,
                                         HttpServletResponse res) {
        String code = body.get("code");
        LoginResponseDto tokens = authService.socialLogin(provider, code, res);
        return ResponseEntity.ok(tokens);
    }

    @Operation(summary = "Access Token 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest req, HttpServletResponse res) {
        String refreshToken = cookieUtils.getRefreshToken(req);
        LoginResponseDto newTokens = authService.refreshAccessToken(refreshToken);

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

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String accessToken, HttpServletResponse res) {
        authService.logout(accessToken);
        cookieUtils.deleteRefreshTokenCookie(res);
        return ResponseEntity.ok("로그아웃 성공");
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