package com.qoocca.teachers.api.attendance.service;

import com.qoocca.teachers.api.global.config.CacheConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceCacheService {

    private final CacheManager cacheManager;

    /**
     * 캐시 삭제를 비동기(@Async)로 처리합니다.
     * 호출한 메인 스레드는 이 작업이 끝날 때까지 기다리지 않고 즉시 응답을 반환합니다.
     */

    public void evictAttendanceCaches(Long academyId, LocalDate date) {
        if (academyId == null || date == null) {
            return;
        }

        String key = academyId + ":" + date;

        // 각 캐시 저장소에서 데이터를 비웁니다.
        Optional.ofNullable(cacheManager.getCache(CacheConfig.ATTENDANCE_SUMMARY))
                .ifPresent(cache -> cache.evict(key));

        Optional.ofNullable(cacheManager.getCache(CacheConfig.ATTENDANCE_TODAY))
                .ifPresent(cache -> cache.evict(key));

        Optional.ofNullable(cacheManager.getCache(CacheConfig.DASHBOARD_STATS))
                .ifPresent(cache -> cache.evict(academyId));
    }
}