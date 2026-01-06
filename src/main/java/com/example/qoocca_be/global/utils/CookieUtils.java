package com.example.qoocca_be.global.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

@Component
public class CookieUtils {
  public void addRefreshTokenCookie(HttpServletResponse res, String refreshToken) {
    Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
    refreshCookie.setHttpOnly(true);
//    refreshCookie.setSecure(true); // HTTPS 통신 시 필수
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge(7 * 24 * 60 * 60);
    res.addCookie(refreshCookie);
  }

  public String getRefreshToken(HttpServletRequest req) {
    if (req.getCookies() != null) {
      for (Cookie cookie : req.getCookies()) {
        if ("refreshToken".equals(cookie.getName())) {
          return cookie.getValue();
        }
      }
    }
    throw new RuntimeException("Refresh Token이 존재하지 않습니다.");
  }

  public void deleteRefreshTokenCookie(HttpServletResponse res) {
    Cookie refreshCookie = new Cookie("refreshToken", null);
    refreshCookie.setHttpOnly(true);
//    refreshCookie.setSecure(true); // HTTPS 통신 시 필수
    refreshCookie.setMaxAge(0);
    refreshCookie.setPath("/");
    res.addCookie(refreshCookie);
  }
}
