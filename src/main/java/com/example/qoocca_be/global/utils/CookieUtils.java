package com.example.qoocca_be.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Component
public class CookieUtils {

    @Value("${jwt.cookie-encryption-key:this-is-a-very-secret-key-32chars}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES";

    public void addRefreshTokenCookie(HttpServletResponse res, String refreshToken) {
        if (refreshToken == null) return;

        try {
            String encryptedToken = encrypt(refreshToken);

            ResponseCookie cookie = ResponseCookie.from("refreshToken", encryptedToken)
                    .httpOnly(true)
                    .secure(false) // 로컬 테스트 시 false
                    .path("/")
                    .maxAge(7 * 24 * 60 * 60)
                    .sameSite("Lax")
                    .build();

            res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        } catch (Exception e) {
            throw new RuntimeException("쿠키 암호화 중 오류 발생", e);
        }
    }

    public String getRefreshToken(HttpServletRequest req) {
        if (req.getCookies() != null) {
            for (Cookie cookie : req.getCookies()) {
                if ("refreshToken".equals(cookie.getName())) {
                    try {
                        return decrypt(cookie.getValue());
                    } catch (Exception e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    // AES 암호화 로직
    private String encrypt(String data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // AES 복호화 로직
    private String decrypt(String encryptedData) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        return new String(cipher.doFinal(decoded));
    }

    public void deleteRefreshTokenCookie(HttpServletResponse res) {
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .build();
        res.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
