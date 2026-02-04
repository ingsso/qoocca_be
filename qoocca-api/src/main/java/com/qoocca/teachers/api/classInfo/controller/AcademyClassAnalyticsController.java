package com.qoocca.teachers.api.classInfo.controller;

import com.qoocca.teachers.api.classInfo.model.response.ClassParentStatsResponse;
import com.qoocca.teachers.api.classInfo.model.response.ClassStatsResponse;
import com.qoocca.teachers.api.classInfo.model.response.ClassSummaryResponse;
import com.qoocca.teachers.api.classInfo.service.ClassInfoService;
import com.qoocca.teachers.api.classInfo.service.ClassInfoStatsService;
import com.qoocca.teachers.api.classInfo.service.ClassParentStatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "학원 분석 API", description = "대시보드/분석용 반 및 보호자 통계 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}")
public class AcademyClassAnalyticsController {

    private final ClassInfoService classInfoService;
    private final ClassInfoStatsService classInfoStatsService;
    private final ClassParentStatsService classParentStatsService;

    @Operation(summary = "대시보드 반 요약 조회", description = "학원 대시보드에 표시할 반별 요약 정보를 조회합니다.")
    @GetMapping("/dashboard/class-summary")
    public ResponseEntity<List<ClassSummaryResponse>> getDashboardClassSummary(@PathVariable Long academyId) {
        return ResponseEntity.ok(classInfoService.getAcademyDashboard(academyId));
    }

    @Operation(summary = "반 통계 조회", description = "학원 반별 세부 통계를 조회합니다.")
    @GetMapping("/analytics/class-stats")
    public ResponseEntity<List<ClassStatsResponse>> getClassStatsAnalytics(@PathVariable Long academyId) {
        return ResponseEntity.ok(classInfoStatsService.getClassStats(academyId));
    }

    @Operation(summary = "보호자 통계 조회", description = "학원 반별 보호자 연계 통계를 조회합니다.")
    @GetMapping("/analytics/parent-stats")
    public ResponseEntity<List<ClassParentStatsResponse>> getParentStatsAnalytics(@PathVariable Long academyId) {
        return ResponseEntity.ok(classParentStatsService.getParentStats(academyId));
    }
}
