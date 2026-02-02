package com.qoocca.teachers.auth.jwt;

import com.qoocca.teachers.auth.service.CustomUserDetailsService;
import com.qoocca.teachers.auth.service.ParentUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final ParentUserDetailsService parentUserDetailsService;

    @Override
    protected void doFilterInternal (HttpServletRequest req, HttpServletResponse res,
                                     FilterChain filterChain)
        throws ServletException, IOException {

        String token = resolveToken(req);

        try {
            if (token != null &&  jwtTokenProvider.validateToken(token)) {
                if (jwtTokenProvider.isBlacklisted(token)) {
                    sendErrorResponse(res, "이미 로그아웃된 토큰입니다.");
                    return;
                }

                String userId = String.valueOf(jwtTokenProvider.getUserIdFromToken(token));
                String role = jwtTokenProvider.getRoleFromToken(token);
                UserDetails userDetails = "ROLE_PARENT".equals(role)
                        ? parentUserDetailsService.loadUserByUsername(userId)
                        : userDetailsService.loadUserByUsername(userId);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            sendErrorResponse(res, "토큰이 만료되었습니다.");
        } catch (JwtException e) {
            sendErrorResponse(res, "유효하지 않은 토큰입니다.");
        }

        filterChain.doFilter(req, res);
    }

    private String resolveToken (HttpServletRequest req) {
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
}
