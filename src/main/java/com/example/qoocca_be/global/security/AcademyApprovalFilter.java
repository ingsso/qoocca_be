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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();


        /*if (path.startsWith("/api/auth") ||
                path.startsWith("/api/dashboard") ||
                path.startsWith("/api/academy/register") ||
                (path.startsWith("/api/academy/") && !path.contains("/class") && request.getMethod().equals("GET"))) {
            filterChain.doFilter(request, response);
            return;
        }*/
        if (path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {

            if (authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                filterChain.doFilter(request, response);
                return;
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            Long userId = userDetails.getUserId();

            Optional<AcademyEntity> academyOpt = academyRepository.findByUserId(userId);

            if (academyOpt.isPresent()) {
                AcademyEntity academy = academyOpt.get();
                if (academy.getApprovalStatus() != ApprovalStatus.APPROVED) {
                    sendErrorResponse(response, ErrorCode.ACADEMY_NOT_APPROVED);
                    return;
                }
            } else {
                sendErrorResponse(response, ErrorCode.ACADEMY_NOT_FOUND);
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