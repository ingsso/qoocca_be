package com.qoocca.teachers.api.classInfo.service;

import com.qoocca.teachers.api.classInfo.model.response.ClassStatsResponse;
import com.qoocca.teachers.api.global.config.CacheConfig;
import com.qoocca.teachers.db.classInfo.entity.StudentStatus;
import com.qoocca.teachers.db.classInfo.model.ClassStatsProjection;
import com.qoocca.teachers.db.classInfo.repository.ClassInfoStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassInfoStatsService {

    private final ClassInfoStatsRepository repository;

    @Cacheable(cacheNames = CacheConfig.ANALYTICS_CLASS_STATS, key = "#academyId")
    public List<ClassStatsResponse> getClassStats(Long academyId) {

        List<ClassStatsProjection> stats = repository.findClassStatsByAcademy(
                academyId,
                StudentStatus.WITHDRAWN
        );

        return stats.stream()
                .map(ClassStatsResponse::from)
                .toList();
    }
}
