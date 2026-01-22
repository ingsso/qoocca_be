package com.qoocca.teachers.classInfo.controller;

import com.qoocca.teachers.classInfo.model.response.ClassParentStatsResponse;
import com.qoocca.teachers.classInfo.service.ClassParentStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Parent Service API", description = "학부모를 위한 자녀 학습 현황 및 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/class")
public class ClassParentStatsController {

    private final ClassParentStatsService service;

    @Operation(
            summary = "학부모용 클래스별 학생 및 보호자 정보 조회",
            description = "특정 학원의 클래스별 수강생 명단과 각 학생에 연결된 보호자(부모) 연락처 정보를 조회합니다."
    )
    @GetMapping("/parentstats")
    public ResponseEntity<List<ClassParentStatsResponse>> getParentStats(
            @Parameter(description = "학원 ID", example = "1") @PathVariable Long academyId
    ) {
        return ResponseEntity.ok(service.getParentStats(academyId));
    }
}