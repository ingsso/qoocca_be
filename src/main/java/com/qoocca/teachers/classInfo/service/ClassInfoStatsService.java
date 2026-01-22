package com.qoocca.teachers.classInfo.service;

import com.qoocca.teachers.classInfo.model.ClassStatsProjection;
import com.qoocca.teachers.classInfo.model.response.ClassStatsResponse;
import com.qoocca.teachers.classInfo.repository.ClassInfoStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassInfoStatsService {

    private final ClassInfoStatsRepository repository;

    public List<ClassStatsResponse> getClassStats(Long academyId) {

        List<ClassStatsProjection> stats = repository.findClassStatsByAcademy(academyId);

        return stats.stream()
                .map(ClassStatsResponse::from)
                .toList();
    }
}
