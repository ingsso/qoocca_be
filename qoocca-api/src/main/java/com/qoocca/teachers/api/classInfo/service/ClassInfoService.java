package com.qoocca.teachers.api.classInfo.service;

import com.qoocca.teachers.api.classInfo.model.request.ClassCreateRequest;
import com.qoocca.teachers.api.classInfo.model.response.ClassCreateResponse;
import com.qoocca.teachers.api.classInfo.model.response.ClassGetResponse;
import com.qoocca.teachers.api.classInfo.model.response.ClassSummaryResponse;
import com.qoocca.teachers.api.global.config.CacheConfig;
import com.qoocca.teachers.auth.security.CustomUserDetails;
import com.qoocca.teachers.common.global.exception.CustomException;
import com.qoocca.teachers.common.global.exception.ErrorCode;
import com.qoocca.teachers.db.academy.entity.AcademyEntity;
import com.qoocca.teachers.db.academy.repository.AcademyRepository;
import com.qoocca.teachers.db.age.entity.AgeEntity;
import com.qoocca.teachers.db.age.repository.AgeRepository;
import com.qoocca.teachers.db.classInfo.entity.ClassInfoEntity;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.model.ClassSummaryDto;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoRepository;
import com.qoocca.teachers.db.subject.entity.SubjectEntity;
import com.qoocca.teachers.db.subject.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ClassInfoService {

    private final ClassInfoRepository classInfoRepository;
    private final AcademyRepository academyRepository;
    private final AgeRepository ageRepository;
    private final SubjectRepository subjectRepository;

    @Caching(evict = {
            @CacheEvict(cacheNames = CacheConfig.ACADEMY_CLASSES, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.DASHBOARD_CLASS_SUMMARY, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_CLASS_STATS, key = "#academyId"),
            @CacheEvict(cacheNames = CacheConfig.ANALYTICS_PARENT_STATS, key = "#academyId")
    })
    public ClassCreateResponse createClass(Long academyId, ClassCreateRequest request, CustomUserDetails userDetails) {
        long startNs = System.nanoTime();
        long academyLookupNs = 0L;
        long ageLookupNs = 0L;
        long subjectLookupNs = 0L;
        long saveNs = 0L;
        boolean success = false;

        try {
            long t0 = System.nanoTime();
            AcademyEntity academy = academyRepository.findById(academyId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ACADEMY_NOT_FOUND));
            academyLookupNs = System.nanoTime() - t0;

            if (!isAdmin(userDetails) && !academy.getUser().getId().equals(userDetails.getUserId())) {
                throw new CustomException(ErrorCode.NO_AUTHORITY);
            }

            t0 = System.nanoTime();
            AgeEntity age = ageRepository.findById(request.getAgeId())
                    .orElseThrow(() -> new CustomException(ErrorCode.AGE_NOT_FOUND));
            ageLookupNs = System.nanoTime() - t0;

            t0 = System.nanoTime();
            SubjectEntity subject = subjectRepository.findById(request.getSubjectId())
                    .orElseThrow(() -> new CustomException(ErrorCode.SUBJECT_NOT_FOUND));
            subjectLookupNs = System.nanoTime() - t0;

            ClassInfoEntity entity = ClassInfoEntity.createClass(
                    request.getClassName(), request.getStartTime(), request.getEndTime(),
                    request.getPrice(), academy, age, subject,
                    request.isMonday(), request.isTuesday(), request.isWednesday(),
                    request.isThursday(), request.isFriday(), request.isSaturday(), request.isSunday()
            );

            t0 = System.nanoTime();
            classInfoRepository.save(entity);
            saveNs = System.nanoTime() - t0;

            success = true;
            return ClassCreateResponse.fromEntity(entity);
        } finally {
            log.info(
                    "class create timing academyId={}, userId={}, success={}, totalMs={}, academyLookupMs={}, ageLookupMs={}, subjectLookupMs={}, saveMs={}",
                    academyId,
                    userDetails == null ? null : userDetails.getUserId(),
                    success,
                    nsToMs(System.nanoTime() - startNs),
                    nsToMs(academyLookupNs),
                    nsToMs(ageLookupNs),
                    nsToMs(subjectLookupNs),
                    nsToMs(saveNs)
            );
        }
    }

    @Cacheable(cacheNames = CacheConfig.ACADEMY_CLASSES, key = "#academyId")
    @Transactional(readOnly = true)
    public List<ClassGetResponse> getClasses(Long academyId) {
        return classInfoRepository.findByAcademy_IdWithDetails(academyId).stream()
                .map(ClassGetResponse::fromEntity)
                .toList();
    }

    @Cacheable(cacheNames = CacheConfig.DASHBOARD_CLASS_SUMMARY, key = "#academyId")
    @Transactional(readOnly = true)
    public List<ClassSummaryResponse> getAcademyDashboard(Long academyId) {
        LocalDate today = LocalDate.now();
        String dayName = today.getDayOfWeek().name();

        List<ClassSummaryDto> dto = classInfoRepository.findDashboardSummaries(
                academyId,
                today,
                dayName,
                StudentStatus.ENROLLED
        );

        return dto.stream()
                .map(ClassSummaryResponse::from)
                .collect(Collectors.toList());
    }

    private boolean isAdmin(CustomUserDetails userDetails) {
        if (userDetails == null) {
            return false;
        }
        return userDetails.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private long nsToMs(long ns) {
        return ns / 1_000_000;
    }
}