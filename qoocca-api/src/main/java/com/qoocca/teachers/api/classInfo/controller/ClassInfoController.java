package com.qoocca.teachers.api.classInfo.controller;

import com.qoocca.teachers.api.classInfo.model.request.ClassCreateRequest;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentMoveRequest;
import com.qoocca.teachers.api.classInfo.model.response.ClassCreateResponse;
import com.qoocca.teachers.api.classInfo.model.response.ClassGetResponse;
import com.qoocca.teachers.api.classInfo.service.ClassInfoService;
import com.qoocca.teachers.api.classInfo.service.ClassInfoStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Class Info API", description = "학원별 클래스 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/class")
public class ClassInfoController {

    private final ClassInfoStudentService classInfoStudentService;
    private final ClassInfoService classInfoService;

    @Operation(summary = "반 생성", description = "학원에 새로운 반을 생성합니다.")
    @PostMapping
    public ResponseEntity<ClassCreateResponse> createClass(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @Valid @RequestBody ClassCreateRequest request
    ) {
        return ResponseEntity.ok(classInfoService.createClass(academyId, request));
    }

    @Operation(summary = "반 목록 조회", description = "학원에 등록된 반 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ClassGetResponse>> getClasses(
            @Parameter(description = "학원 ID") @PathVariable Long academyId
    ) {
        return ResponseEntity.ok(classInfoService.getClasses(academyId));
    }

    @Operation(summary = "학생 반 이동", description = "학생을 같은 학원 내 다른 반으로 이동시킵니다.")
    @PutMapping("/{classId}/student/{studentId}/move")
    public ResponseEntity<Void> moveStudent(
            @Parameter(description = "학원 ID") @PathVariable Long academyId,
            @Parameter(description = "현재 반 ID") @PathVariable Long classId,
            @Parameter(description = "학생 ID") @PathVariable Long studentId,
            @Valid @RequestBody ClassStudentMoveRequest request
    ) {
        classInfoStudentService.moveStudent(academyId, classId, studentId, request.getTargetClassId());
        return ResponseEntity.ok().build();
    }
}
