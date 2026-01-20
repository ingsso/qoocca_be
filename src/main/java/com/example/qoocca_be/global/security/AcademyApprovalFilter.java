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
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AcademyApprovalFilter extends OncePerRequestFilter {

    private final AcademyRepository academyRepository;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private static final List<String> EXCLUDE_URLS = List.of(
            "/api/auth/**",
            "/api/ages/**",
            "/api/subjects/**",
            "/api/academy/register",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/academy/*/class/*/student/*/move"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 학원 목록 조회는 항상 허용 (모달 때문에)
        if (path.equals("/api/academy") && request.getMethod().equals("GET")) {
            return true;
        }

        return EXCLUDE_URLS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isAuthenticatedUser(authentication)) {

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // ADMIN 프리패스
            if (hasRole(authentication, "ROLE_ADMIN")) {
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = userDetails.getUserId();

            List<AcademyEntity> academies = academyRepository.findAllByUserId(userId);

            if (academies.isEmpty()) {
                sendErrorResponse(response, ErrorCode.ACADEMY_NOT_FOUND);
                return;
            }

            // academy/{id} 접근 시 소유권 체크
            if (request.getMethod().equals("GET")
                    && pathMatcher.match("/api/academy/{id}", request.getRequestURI())) {

                Map<String, String> variables =
                        pathMatcher.extractUriTemplateVariables("/api/academy/{id}", request.getRequestURI());

                Long requestedAcademyId = Long.parseLong(variables.get("id"));

                boolean ownsAcademy = academies.stream()
                        .anyMatch(a -> a.getId().equals(requestedAcademyId));

                if (!ownsAcademy) {
                    sendErrorResponse(response, ErrorCode.ACADEMY_NOT_FOUND);
                    return;
                }
            }

            // 승인된 학원 접근 제한 (목록 제외)
            boolean hasApprovedAcademy = academies.stream()
                    .anyMatch(a -> a.getApprovalStatus() == ApprovalStatus.APPROVED);

            if (!hasApprovedAcademy) {
                sendErrorResponse(response, ErrorCode.ACADEMY_NOT_APPROVED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAuthenticatedUser(Authentication auth) {
        return auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof CustomUserDetails;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
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
