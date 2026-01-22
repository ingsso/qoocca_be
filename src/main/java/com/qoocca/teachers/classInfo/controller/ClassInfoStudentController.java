package com.qoocca.teachers.classInfo.controller;

import com.qoocca.teachers.classInfo.model.request.ClassStudentRequest;
import com.qoocca.teachers.classInfo.model.response.ClassStudentResponse;
import com.qoocca.teachers.classInfo.service.ClassInfoStudentService;
import com.qoocca.teachers.classInfo.model.request.ClassStudentModifyRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Class Student API", description = "클래스 수강생 관리 API (배정, 상태 변경, 조회, 삭제)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/class/{classId}/student")
public class ClassInfoStudentController {

    private final ClassInfoStudentService service;

    @Operation(summary = "클래스에 학생 등록", description = "기존에 등록된 학생을 특정 클래스의 수강생으로 배정합니다.")
    @PostMapping
    public ResponseEntity<Void> register(
            @Parameter(description = "클래스 ID", example = "1") @PathVariable Long classId,
            @RequestBody ClassStudentRequest request
    ) {
        service.register(classId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "학생 수강 상태 수정", description = "특정 클래스를 수강 중인 학생의 상태(재원, 퇴원, 휴원 등)를 변경합니다.")
    @PutMapping("/{studentId}")
    public ResponseEntity<Void> modifyStatus(
            @Parameter(description = "클래스 ID", example = "1") @PathVariable Long classId,
            @Parameter(description = "학생 ID", example = "10") @PathVariable Long studentId,
            @RequestBody ClassStudentModifyRequest request
    ) {
        service.modifyStatus(classId, studentId, request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "클래스별 수강생 목록 조회", description = "해당 클래스에 배정된 모든 학생의 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<List<ClassStudentResponse>> getStudents(
            @Parameter(description = "클래스 ID", example = "1") @PathVariable Long classId
    ) {
        return ResponseEntity.ok(service.getStudents(classId));
    }

    @Operation(summary = "클래스에서 학생 제외", description = "해당 클래스의 수강 명단에서 학생을 삭제합니다.")
    @DeleteMapping("/{studentId}")
    public ResponseEntity<Void> remove(
            @Parameter(description = "클래스 ID", example = "1") @PathVariable Long classId,
            @Parameter(description = "학생 ID", example = "10") @PathVariable Long studentId
    ) {
        service.remove(classId, studentId);
        return ResponseEntity.ok().build();
    }
}