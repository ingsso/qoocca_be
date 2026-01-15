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
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AcademyApprovalFilter extends OncePerRequestFilter {
    private final AcademyRepository academyRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 인증/공개 API는 패스
        if (path.startsWith("/api/auth") ||
                path.startsWith("/swagger") ||
                path.startsWith("/v3/api-docs")) {

            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomUserDetails userDetails) {

            // ✅ ADMIN 은 무조건 통과
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

            if (isAdmin) {
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = userDetails.getUserId();

            Optional<AcademyEntity> academyOpt = academyRepository.findByUserId(userId);

            if (academyOpt.isEmpty()) {
                sendErrorResponse(response, ErrorCode.ACADEMY_NOT_FOUND);
                return;
            }

            AcademyEntity academy = academyOpt.get();

            if (academy.getApprovalStatus() != ApprovalStatus.APPROVED) {
                sendErrorResponse(response, ErrorCode.ACADEMY_NOT_APPROVED);
                return;
            }
        }

        filterChain.doFilter(request, response);
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