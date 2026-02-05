package com.qoocca.teachers.api.classInfo.controller;

import com.qoocca.teachers.api.classInfo.model.request.ClassStudentModifyRequest;
import com.qoocca.teachers.api.classInfo.model.request.ClassStudentRequest;
import com.qoocca.teachers.api.classInfo.model.response.ClassStudentResponse;
import com.qoocca.teachers.api.classInfo.service.ClassInfoStudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Class Student API", description = "클래스 수강생 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/academy/{academyId}/class/{classId}/student")
public class ClassInfoStudentController {

    private final ClassInfoStudentService service;

    @Operation(summary = "학생 등록", description = "기존 학생을 클래스 수강생으로 등록합니다.")
    @PostMapping
    public ResponseEntity<Void> register(
            @Parameter(description = "학원 ID", example = "1") @PathVariable Long academyId,
            @Parameter(description = "클래스 ID", example = "1") @PathVariable Long classId,
            @Valid @RequestBody ClassStudentRequest request
    ) {
        service.register(academyId, classId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "수강 상태 수정", description = "클래스 내 학생 수강 상태를 수정합니다.")
    @PutMapping("/{studentId}")
    public ResponseEntity<Void> modifyStatus(
            @Parameter(description = "학원 ID", example = "1") @PathVariable Long academyId,
            @Parameter(description = "클래스 ID", example = "1") @PathVariable Long classId,
            @Parameter(description = "학생 ID", example = "10") @PathVariable Long studentId,
            @Valid @RequestBody ClassStudentModifyRequest request
    ) {
        service.modifyStatus(academyId, classId, studentId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "수강생 목록 조회", description = "클래스에 등록된 수강생 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ClassStudentResponse>> getStudents(
            @Parameter(description = "학원 ID", example = "1") @PathVariable Long academyId,
            @Parameter(description = "클래스 ID", example = "1") @PathVariable Long classId
    ) {
        return ResponseEntity.ok(service.getStudents(academyId, classId));
    }

    @Operation(summary = "학생 제외", description = "클래스 수강 명단에서 학생을 제외합니다.")
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> remove(
            @Parameter(description = "학원 ID", example = "1") @PathVariable Long academyId,
            @Parameter(description = "클래스 ID", example = "1") @PathVariable Long classId,
            @Parameter(description = "학생 ID", example = "10") @PathVariable Long studentId
    ) {
        service.remove(academyId, classId, studentId);
        return ResponseEntity.ok().build();
    }
}
