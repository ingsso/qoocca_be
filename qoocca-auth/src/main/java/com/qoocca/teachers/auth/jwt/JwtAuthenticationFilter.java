package com.qoocca.teachers.auth.jwt;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.qoocca.teachers.auth.service.CustomUserDetailsService;
import com.qoocca.teachers.auth.service.ParentUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ParentUserDetailsService parentUserDetailsService;
    private final Cache<String, UserDetails> userDetailsCache;

    @Value("${auth.user-cache.ttl-ms:30000}")
    private long userCacheTtlMs;

    @Value("${auth.user-cache.max-size:10000}")
    private long userCacheMaxSize;

    @Value("${auth.filter.verbose-log:false}")
    private boolean verboseLog;

    public JwtAuthenticationFilter(
            JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService userDetailsService,
            ParentUserDetailsService parentUserDetailsService,
            @Value("${auth.user-cache.ttl-ms:30000}") long userCacheTtlMs,
            @Value("${auth.user-cache.max-size:10000}") long userCacheMaxSize
    ) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.parentUserDetailsService = parentUserDetailsService;
        this.userCacheTtlMs = userCacheTtlMs;
        this.userCacheMaxSize = userCacheMaxSize;
        this.userDetailsCache = Caffeine.newBuilder()
                .maximumSize(userCacheMaxSize)
                .expireAfterWrite(userCacheTtlMs, TimeUnit.MILLISECONDS)
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
            throws ServletException, IOException {

        long startNs = System.nanoTime();
        long blacklistNs = 0L;
        long userLoadNs = 0L;
        boolean userCacheHit = false;
        String principalKey = null;

        String token = resolveToken(req);

        try {
            if (token != null && jwtTokenProvider.validateToken(token)) {
                long t0 = System.nanoTime();
                if (jwtTokenProvider.isBlacklisted(token)) {
                    sendErrorResponse(res, "Unauthorized token");
                    return;
                }
                blacklistNs = System.nanoTime() - t0;

                String userId = String.valueOf(jwtTokenProvider.getUserIdFromToken(token));
                String role = jwtTokenProvider.getRoleFromToken(token);
                principalKey = role + ":" + userId;

                t0 = System.nanoTime();
                boolean wasCached = userDetailsCache.getIfPresent(principalKey) != null;
                UserDetails userDetails = getUserDetails(principalKey, role, userId);
                userLoadNs = System.nanoTime() - t0;
                userCacheHit = wasCached;

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            sendErrorResponse(res, "Token expired");
            return;
        } catch (JwtException e) {
            sendErrorResponse(res, "Invalid token");
            return;
        }

        try {
            filterChain.doFilter(req, res);
        } finally {
            if (verboseLog && token != null) {
                log.info(
                        "jwt filter timing path={}, principalKey={}, userCacheHit={}, totalMs={}, blacklistMs={}, userLoadMs={}",
                        req.getRequestURI(),
                        principalKey,
                        userCacheHit,
                        nsToMs(System.nanoTime() - startNs),
                        nsToMs(blacklistNs),
                        nsToMs(userLoadNs)
                );
            }
        }
    }

    private String resolveToken(HttpServletRequest req) {
        String bearer = req.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse res, String message) throws IOException {
        res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write(String.format("{\"error\": \"%s\", \"message\": \"%s\"}", "UNAUTHORIZED", message));
    }

    private UserDetails getUserDetails(String principalKey, String role, String userId) {
        UserDetails cached = userDetailsCache.getIfPresent(principalKey);
        if (cached != null) {
            return cached;
        }

        UserDetails loaded = "ROLE_PARENT".equals(role)
                ? parentUserDetailsService.loadUserByUsername(userId)
                : userDetailsService.loadUserByUsername(userId);
        userDetailsCache.put(principalKey, loaded);
        return loaded;
    }

    private long nsToMs(long ns) {
        return ns / 1_000_000;
    }

}
