package com.qoocca.teachers.common.global.utils;

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

    @Value("${security.cookie.refresh.secure:false}")
    private boolean refreshCookieSecure;

    @Value("${security.cookie.refresh.same-site:Lax}")
    private String refreshCookieSameSite;

    @Value("${security.cookie.refresh.path:/}")
    private String refreshCookiePath;

    @Value("${security.cookie.refresh.domain:}")
    private String refreshCookieDomain;

    @Value("${security.cookie.refresh.max-age:604800}")
    private long refreshCookieMaxAge;

    private static final String ALGORITHM = "AES";

    public void addRefreshTokenCookie(HttpServletResponse res, String refreshToken) {
        if (refreshToken == null) return;

        try {
            String encryptedToken = encrypt(refreshToken);

            ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", encryptedToken)
                    .httpOnly(true)
                    .secure(refreshCookieSecure)
                    .path(refreshCookiePath)
                    .maxAge(refreshCookieMaxAge)
                    .sameSite(refreshCookieSameSite);

            if (!refreshCookieDomain.isBlank()) {
                cookieBuilder.domain(refreshCookieDomain);
            }

            res.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt refresh token cookie", e);
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

    private String encrypt(String data) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private String decrypt(String encryptedData) throws Exception {
        SecretKeySpec keySpec = new SecretKeySpec(encryptionKey.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        return new String(cipher.doFinal(decoded));
    }

    public void deleteRefreshTokenCookie(HttpServletResponse res) {
        ResponseCookie.ResponseCookieBuilder cookieBuilder = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(refreshCookieSecure)
                .path(refreshCookiePath)
                .maxAge(0)
                .sameSite(refreshCookieSameSite);

        if (!refreshCookieDomain.isBlank()) {
            cookieBuilder.domain(refreshCookieDomain);
        }

        res.addHeader(HttpHeaders.SET_COOKIE, cookieBuilder.build().toString());
    }
}
