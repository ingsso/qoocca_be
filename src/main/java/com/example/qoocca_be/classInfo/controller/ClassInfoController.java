package com.example.qoocca_be.classInfo.controller;

import com.example.qoocca_be.classInfo.model.request.ClassStudentMoveRequest;
import com.example.qoocca_be.classInfo.model.request.ClassCreateRequest;
import com.example.qoocca_be.classInfo.model.response.ClassGetResponse;
import com.example.qoocca_be.classInfo.model.response.ClassCreateResponse;
import com.example.qoocca_be.classInfo.model.response.ClassStatsResponse;
import com.example.qoocca_be.classInfo.model.response.ClassSummaryResponse;
import com.example.qoocca_be.classInfo.service.ClassInfoService;
import com.example.qoocca_be.classInfo.service.ClassInfoStatsService;
import com.example.qoocca_be.classInfo.service.ClassInfoStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Class Info API", description = "학원 내 클래스 운영 및 수강 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/class")
public class ClassInfoController {

    private final ClassInfoStudentService classInfoStudentService;
    private final ClassInfoStatsService classInfoStatsService;
    private final ClassInfoService classInfoService;

    /* =========================
     * 클래스 등록
     * ========================= */
    @Operation(summary = "클래스 생성", description = "학원에 새로운 수업 클래스를 등록합니다.")
    @PostMapping
    public ResponseEntity<ClassCreateResponse> createClass(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @RequestBody ClassCreateRequest request
    ) {
        return ResponseEntity.ok(
                classInfoService.createClass(academyId, request)
        );
    }

    /* =========================
     * 클래스 목록 조회
     * ========================= */
    @Operation(summary = "학원별 클래스 전체 조회", description = "해당 학원에 개설된 모든 클래스 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ClassGetResponse>> getClasses(
            @Parameter(description = "학원 ID") @PathVariable Long academyId
    ) {
        return ResponseEntity.ok(
                classInfoService.getClasses(academyId)
        );
    }

    @Operation(summary = "수강생 반 이동", description = "학생을 현재 클래스에서 다른 클래스로 이동시킵니다.")
    @PutMapping("/{classId}/student/{studentId}/move")
    public ResponseEntity<Void> moveStudent(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @Parameter(description = "현재 클래스 ID") @PathVariable Long classId,
            @Parameter(description = "학생 ID") @PathVariable Long studentId,
            @RequestBody ClassStudentMoveRequest request
    ) {
        classInfoStudentService.moveStudent(
                classId,
                studentId,
                request.getTargetClassId()
        );
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "학원 대시보드용 클래스 요약 조회", description = "학원 메인 대시보드에 표시할 클래스별 요약 정보(수업 시간 등)를 조회합니다.")
    @GetMapping("/summary")
    public ResponseEntity<List<ClassSummaryResponse>> getAcademyClassSummaries(@PathVariable Long academyId) {
        List<ClassSummaryResponse> summaries = classInfoService.getAcademyDashboard(academyId);

        return ResponseEntity.ok(summaries);
    }

    @Operation(summary = "클래스별 운영 통계 조회", description = "학원 내 클래스들의 통계 데이터(인원 현황 등)를 조회합니다.")
    @GetMapping("/stats")
    public ResponseEntity<List<ClassStatsResponse>> getClassStats(
            @PathVariable Long academyId
    ) {
        return ResponseEntity.ok(classInfoStatsService.getClassStats(academyId));
    }
}
