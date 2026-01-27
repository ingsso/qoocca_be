package com.qoocca.teachers.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qoocca.teachers.auth.security.CustomUserDetails;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.common.global.exception.ErrorResponse;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.entity.ApprovalStatus;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
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
            "/api/academy/*/resubmit",
            "/api/academy/complete",
            "/api/academy/academy-list",
            "/api/attendance/**",
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

        if (!isAuthenticatedUser(authentication)) {
            filterChain.doFilter(request, response);
            return;
        }

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if (hasRole(authentication, "ROLE_ADMIN")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestUri = request.getRequestURI();
        List<AcademyEntity> myAcademies = academyRepository.findAllByUserId(userDetails.getUserId());

        if (request.getMethod().equals("GET") && pathMatcher.match("/api/academy/{id}", requestUri)) {
            Map<String, String> variables = pathMatcher.extractUriTemplateVariables("/api/academy/{id}", requestUri);
            String idStr = variables.get("id");

            if (idStr != null) {
                Long requestedAcademyId = Long.parseLong(idStr);
                boolean isMine = myAcademies.stream()
                        .anyMatch(a -> a.getId().equals(requestedAcademyId));

                if (isMine) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }
        }

        if (myAcademies.isEmpty()) {
            sendErrorResponse(response, ErrorCode.ACADEMY_NOT_FOUND);
            return;
        }

        boolean hasApprovedAcademy = myAcademies.stream()
                .anyMatch(a -> a.getApprovalStatus() == ApprovalStatus.APPROVED);

        if (!hasApprovedAcademy) {
            sendErrorResponse(response, ErrorCode.ACADEMY_NOT_APPROVED);
            return;
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
