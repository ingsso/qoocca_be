package com.qoocca.teachers.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class AcademyApprovalFilter extends OncePerRequestFilter {

    private final AcademyRepository academyRepository;
    private final ObjectMapper objectMapper;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final Cache<Long, ApprovalSnapshot> approvalCache;

    @Value("${auth.approval-cache.ttl-ms:30000}")
    private long approvalCacheTtlMs;

    @Value("${auth.approval-cache.max-size:10000}")
    private long approvalCacheMaxSize;

    @Value("${auth.filter.verbose-log:false}")
    private boolean verboseLog;

    public AcademyApprovalFilter(
            AcademyRepository academyRepository,
            ObjectMapper objectMapper,
            @Value("${auth.approval-cache.ttl-ms:30000}") long approvalCacheTtlMs,
            @Value("${auth.approval-cache.max-size:10000}") long approvalCacheMaxSize
    ) {
        this.academyRepository = academyRepository;
        this.objectMapper = objectMapper;
        this.approvalCacheTtlMs = approvalCacheTtlMs;
        this.approvalCacheMaxSize = approvalCacheMaxSize;
        this.approvalCache = Caffeine.newBuilder()
                .maximumSize(approvalCacheMaxSize)
                .expireAfterWrite(approvalCacheTtlMs, TimeUnit.MILLISECONDS)
                .build();
    }

    private static final List<String> EXCLUDE_URLS = List.of(
            "/api/auth/**",
            "/api/admin/**",
            "/api/ages/**",
            "/api/subjects/**",
            "/api/academy/registrations",
            "/api/academy/*/files",
            "/api/academy/*/approval/resubmissions",
            "/api/me/**",
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
        long startNs = System.nanoTime();
        long lookupNs = 0L;
        boolean cacheHit = false;
        Long userId = null;

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (!isAuthenticatedUser(authentication)) {
                filterChain.doFilter(request, response);
                return;
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            userId = userDetails.getUserId();

            if (hasRole(authentication, "ROLE_ADMIN")) {
                filterChain.doFilter(request, response);
                return;
            }

            String requestUri = request.getRequestURI();
            long t0 = System.nanoTime();
            SnapshotResult snapshotResult = getApprovalSnapshot(userId);
            lookupNs = System.nanoTime() - t0;
            cacheHit = snapshotResult.cacheHit;
            ApprovalSnapshot snapshot = snapshotResult.snapshot;

            if (request.getMethod().equals("GET") && pathMatcher.match("/api/academy/{id}", requestUri)) {
                Map<String, String> variables = pathMatcher.extractUriTemplateVariables("/api/academy/{id}", requestUri);
                Long requestedAcademyId = parseLongPathVar(variables.get("id"));
                if (requestedAcademyId != null && snapshot.academyIds.contains(requestedAcademyId)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            if (request.getMethod().equals("GET") && pathMatcher.match("/api/academy/{id}/profile", requestUri)) {
                Map<String, String> variables = pathMatcher.extractUriTemplateVariables("/api/academy/{id}/profile", requestUri);
                Long requestedAcademyId = parseLongPathVar(variables.get("id"));
                if (requestedAcademyId != null && snapshot.academyIds.contains(requestedAcademyId)) {
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            if (!snapshot.hasAnyAcademy) {
                sendErrorResponse(response, ErrorCode.ACADEMY_NOT_FOUND);
                return;
            }

            if (!snapshot.hasApprovedAcademy) {
                sendErrorResponse(response, ErrorCode.ACADEMY_NOT_APPROVED);
                return;
            }

            filterChain.doFilter(request, response);
        } finally {
            if (verboseLog && userId != null) {
                log.info(
                        "academy approval filter timing path={}, userId={}, cacheHit={}, totalMs={}, snapshotLookupMs={}",
                        request.getRequestURI(),
                        userId,
                        cacheHit,
                        nsToMs(System.nanoTime() - startNs),
                        nsToMs(lookupNs)
                );
            }
        }
    }

    private boolean isAuthenticatedUser(Authentication auth) {
        return auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof CustomUserDetails;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(role));
    }

    private Long parseLongPathVar(String value) {
        if (value == null) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
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

    private SnapshotResult getApprovalSnapshot(Long userId) {
        ApprovalSnapshot cached = approvalCache.getIfPresent(userId);
        if (cached != null) {
            return new SnapshotResult(cached, true);
        }

        List<AcademyEntity> academies = academyRepository.findAllByUserId(userId);
        Set<Long> academyIds = academies.stream().map(AcademyEntity::getId).collect(java.util.stream.Collectors.toSet());
        boolean hasApproved = academies.stream().anyMatch(a -> a.getApprovalStatus() == ApprovalStatus.APPROVED);
        ApprovalSnapshot fresh = new ApprovalSnapshot(!academies.isEmpty(), hasApproved, academyIds);
        approvalCache.put(userId, fresh);
        return new SnapshotResult(fresh, false);
    }

    private long nsToMs(long ns) {
        return ns / 1_000_000;
    }

    private record ApprovalSnapshot(
            boolean hasAnyAcademy,
            boolean hasApprovedAcademy,
            Set<Long> academyIds
    ) {
    }

    private record SnapshotResult(ApprovalSnapshot snapshot, boolean cacheHit) {
    }
}
