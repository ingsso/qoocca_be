package com.example.qoocca_be.user.security.filter;

import com.example.qoocca_be.user.security.util.JwtTokenProvider;
import com.example.qoocca_be.user.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal (HttpServletRequest req, HttpServletResponse res,
                                     FilterChain filterChain)
        throws ServletException, IOException {

        String token = resolveToken(req);

        try {
            if (token != null &&  jwtTokenProvider.validateToken(token)) {
                if (jwtTokenProvider.isBlacklisted(token)) {
                    res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    return;
                }

                String identifier = jwtTokenProvider.getIdentifierFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(identifier);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (ExpiredJwtException e) {
            req.setAttribute("exception", "EXPIRED_TOKEN");
        } catch (JwtException e) {
            req.setAttribute("exception", "INVALID_TOKEN");
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
}
