package com.example.qoocca_be.global.security;

import com.example.qoocca_be.academy.entity.AcademyEntity;
import com.example.qoocca_be.academy.entity.ApprovalStatus;
import com.example.qoocca_be.academy.repository.AcademyRepository;
import com.example.qoocca_be.global.exception.ErrorCode;
import com.example.qoocca_be.global.exception.ErrorResponse;
import com.example.qoocca_be.user.security.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AcademyApprovalFilter extends OncePerRequestFilter {
    private final AcademyRepository academyRepository;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    // 필터를 적용하지 않을 경로를 명확히 정의
    private static final List<String> EXCLUDE_URLS = List.of(
            "/api/auth/**",
            "/api/ages/**",
            "/api/subjects/**",
            "/api/academy/register",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/academy/*/class/*/student/*/move" // 패턴 매칭 지원
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDE_URLS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증된 사용자이고, 일반 사용자(ROLE_USER 등)인 경우에만 체크
        if (isAuthenticatedUser(authentication)) {
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // ROLE_ADMIN 은 승인 여부와 상관없이 프리패스
            if (hasRole(authentication, "ROLE_ADMIN")) {
                filterChain.doFilter(request, response);
                return;
            }

            // JWT에 상태를 담았다면 여기서 바로 userDetails.getApprovalStatus() 체크 가능
            AcademyEntity academy = academyRepository.findByUserId(userDetails.getUserId())
                    .orElseThrow(() -> {
                        // 학원 정보가 없는 경우에 대한 명확한 에러 처리
                        try {
                            sendErrorResponse(response, ErrorCode.ACADEMY_NOT_FOUND);
                        } catch (IOException e) { e.printStackTrace(); }
                        return null;
                    });

            if (academy != null && academy.getApprovalStatus() != ApprovalStatus.APPROVED) {
                sendErrorResponse(response, ErrorCode.ACADEMY_NOT_APPROVED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticatedUser(Authentication auth) {
        return auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }

    private void sendErrorResponse(HttpServletResponse res, ErrorCode errorCode) throws IOException {
        res.setStatus(errorCode.getStatus());
        res.setContentType("application/json;charset=UTF-8");

        ErrorResponse errorResponse = ErrorResponse.builder()
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        res.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}