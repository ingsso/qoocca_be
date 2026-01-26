package com.qoocca.teachers.api.global.jwt;

import com.qoocca.teachers.api.user.model.LoginResponse;
import com.qoocca.teachers.api.user.model.RedisDao;
import com.qoocca.teachers.common.global.utils.CookieUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final Key key;
    private final RedisDao redisDao;
    private final CookieUtils cookieUtils;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    @Value("${jwt.access-token-expiration}")
    private long accessTokenExpireTime;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpireTime;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey,
                            RedisDao redisDao,
                            CookieUtils cookieUtils) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisDao = redisDao;
        this.cookieUtils = cookieUtils;
    }

    public LoginResponse generateTokens(Long userId, String role, HttpServletResponse res) {
        String accessToken = generateAccessToken(userId, role);
        String refreshToken = generateRefreshToken(userId, role);

        cookieUtils.addRefreshTokenCookie(res, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(null)
                .build();
    }

    public String generateAccessToken(Long userId, String role){
        Claims claims = Jwts.claims().setSubject(String.valueOf(userId));
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessTokenExpireTime))
                .signWith(key)
                .compact();
    }

    public String generateRefreshToken(Long userId, String role){
        String subject = String.valueOf(userId);
        Claims claims = Jwts.claims().setSubject(subject);
        claims.put("role", role);

        String refreshToken =  Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshTokenExpireTime))
                .signWith(key)
                .compact();

        redisDao.setValues(subject, refreshToken, Duration.ofMillis(refreshTokenExpireTime));

        return refreshToken;
    }

    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return (String) claims.get("role");
        } catch (ExpiredJwtException e) {
            return (String) e.getClaims().get("role");
        }
    }

    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return Long.parseLong(claims.getSubject());
        } catch (ExpiredJwtException e) {
            return Long.parseLong(e.getClaims().getSubject());
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateRefreshToken(String token) {
        if (!validateToken(token)) return false;

        try {
            Long userId = getUserIdFromToken(token);
            String redisToken = (String) redisDao.getValues(String.valueOf(userId));
            return token.equals(redisToken);
        } catch (Exception e) {
            return false;
        }
    }

    public void deleteRefreshToken(Long userId) {
        redisDao.deleteValues(String.valueOf(userId));
    }

    public void addToBlacklist(String accessToken) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(accessToken)
                    .getBody();

            Date expiration = claims.getExpiration();
            long now = System.currentTimeMillis();

            long remainTime = expiration.getTime() - now;

            if (remainTime > 0) {
                redisDao.setValues(BLACKLIST_PREFIX + accessToken, "logout", Duration.ofMillis(remainTime));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isBlacklisted(String accessToken) {
        String key = BLACKLIST_PREFIX + accessToken;
        return redisDao.getValues(key) != null;
    }
}
